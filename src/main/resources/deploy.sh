#!/bin/bash

set -e

# === CONFIGURATION ===
KEY_NAME="springboot-key"
SECURITY_GROUP_NAME="springboot-sg"
INSTANCE_NAME="springboot-instance"
REGION="us-east-1"
AMI_ID="ami-0e449927258d45bc4" 
INSTANCE_TYPE="t2.micro"
LOG_GROUP_NAME="ec2-springboot"
GITHUB_USERNAME="Bablu7011"
GITHUB_PAT="add github pat here"
REPO_NAME="Springbootproject"

echo "Starting setup..."

# === KEY PAIR SETUP ===
echo "Checking key pair..."
if aws ec2 describe-key-pairs --key-names $KEY_NAME --region $REGION 2>/dev/null; then
  echo "Key pair $KEY_NAME already exists. Skipping creation."
else
  echo "Creating key pair $KEY_NAME..."
  aws ec2 create-key-pair --key-name $KEY_NAME --query 'KeyMaterial' --output text --region $REGION > $KEY_NAME.pem
  chmod 400 $KEY_NAME.pem
fi

# === SECURITY GROUP SETUP ===
echo "Checking security group..."
EXISTING_SG_ID=$(aws ec2 describe-security-groups --group-names $SECURITY_GROUP_NAME --region $REGION --query 'SecurityGroups[0].GroupId' --output text 2>/dev/null || echo "")
if [ -n "$EXISTING_SG_ID" ] && [ "$EXISTING_SG_ID" != "None" ]; then
  echo "Security group $SECURITY_GROUP_NAME already exists. Skipping creation."
  SG_ID="$EXISTING_SG_ID"
else
  echo "Creating security group $SECURITY_GROUP_NAME..."
  SG_ID=$(aws ec2 create-security-group \
    --group-name $SECURITY_GROUP_NAME \
    --description "Security group for Spring Boot app" \
    --region $REGION \
    --query 'GroupId' --output text)

  echo "Authorizing security group ingress rules..."
  aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 22 --cidr 0.0.0.0/0 --region $REGION
  aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 80 --cidr 0.0.0.0/0 --region $REGION
fi

# === CLOUDWATCH LOG GROUP SETUP ===
echo "Checking CloudWatch log group..."
if aws logs describe-log-groups --log-group-name-prefix $LOG_GROUP_NAME --region $REGION | grep "$LOG_GROUP_NAME" >/dev/null; then
  echo "Log group $LOG_GROUP_NAME already exists. Skipping creation."
else
  echo "Creating log group $LOG_GROUP_NAME..."
  aws logs create-log-group --log-group-name $LOG_GROUP_NAME --region $REGION
fi

# === EC2 INSTANCE SETUP ===
echo "Checking for existing EC2 instance..."
EXISTING_INSTANCE_ID=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=$INSTANCE_NAME" "Name=instance-state-name,Values=running,stopped" \
  --region $REGION \
  --query 'Reservations[].Instances[].InstanceId' --output text)

NEW_INSTANCE_CREATED=false
if [ -n "$EXISTING_INSTANCE_ID" ] && [ "$EXISTING_INSTANCE_ID" != "None" ]; then
  echo "Instance $INSTANCE_NAME already exists. Skipping creation."
  INSTANCE_ID=$EXISTING_INSTANCE_ID
else
  echo "Launching new EC2 instance..."
  USER_DATA_SCRIPT=$(cat <<EOF
#!/bin/bash
exec > >(tee /home/ec2-user/setup.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "Updating system..."
yum update -y
echo "Installing Java 17..."
yum install java-17-amazon-corretto-devel -y
echo "Installing Git..."
yum install git -y
echo "Installing Maven..."
cd /opt
curl -O https://archive.apache.org/dist/maven/maven-3/3.9.1/binaries/apache-maven-3.9.1-bin.tar.gz
tar -xvzf apache-maven-3.9.1-bin.tar.gz
export M2_HOME=/opt/apache-maven-3.9.1
export PATH=\$M2_HOME/bin:\$PATH
echo "Verifying Java and Maven..."
java -version
mvn -version

echo "Cloning GitHub repository..."
cd /home/ec2-user
git clone https://${GITHUB_USERNAME}:${GITHUB_PAT}@github.com/${GITHUB_USERNAME}/${REPO_NAME}.git

echo "Building Spring Boot project..."
cd ${REPO_NAME}
mvn clean install

echo "Running JAR file..."
cd target
JAR_FILE=\$(ls *.jar | grep -v 'original' | head -n 1)
nohup java -jar "\$JAR_FILE" > /home/ec2-user/app.log 2>&1 &

echo "Installing CloudWatch agent..."
yum install -y amazon-cloudwatch-agent

echo "Creating CloudWatch agent config..."
cat <<EOC > /opt/cloudwatch-config.json
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/home/ec2-user/app.log",
            "log_group_name": "${LOG_GROUP_NAME}",
            "log_stream_name": "{instance_id}",
            "timestamp_format": "%Y-%m-%d %H:%M:%S"
          }
        ]
      }
    }
  }
}
EOC

echo "Starting CloudWatch agent..."
/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \\
  -a fetch-config -m ec2 -c file:/opt/cloudwatch-config.json -s

echo "Setup complete."
EOF
)

  INSTANCE_ID=$(aws ec2 run-instances \
    --image-id $AMI_ID \
    --instance-type $INSTANCE_TYPE \
    --key-name $KEY_NAME \
    --security-group-ids $SG_ID \
    --user-data "$USER_DATA_SCRIPT" \
    --region $REGION \
    --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$INSTANCE_NAME}]" \
    --query 'Instances[0].InstanceId' \
    --output text)

  echo "Waiting for instance to be running..."
  aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region $REGION
  NEW_INSTANCE_CREATED=true
fi

# === FETCH PUBLIC IP ===
PUBLIC_IP=$(aws ec2 describe-instances \
  --instance-ids $INSTANCE_ID \
  --region $REGION \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text)

echo "Instance is live at http://$PUBLIC_IP:80"

# === SSH AND HASH CHECK (only if instance already existed) ===
if [ "$NEW_INSTANCE_CREATED" = false ]; then
  echo "Checking if app needs to be updated..."

  # Fetch old hash from EC2
  OLD_HASH=$(ssh -o StrictHostKeyChecking=no -i "$KEY_NAME.pem" ec2-user@"$PUBLIC_IP" "
    if [ -f /home/ec2-user/$REPO_NAME/target/*.jar ]; then
      cd /home/ec2-user/$REPO_NAME/target
      JAR_FILE=\$(ls *.jar | grep -v 'original' | head -n 1)
      sha256sum \"\$JAR_FILE\" | awk '{print \$1}'
    else
      echo 'nojar'
    fi
  ")

  echo "Old JAR hash on EC2: $OLD_HASH"

  # Fetch new hash locally from GitHub latest code
  echo "Cloning GitHub repository locally to calculate new hash..."
  rm -rf temp_repo
  git clone https://${GITHUB_USERNAME}:${GITHUB_PAT}@github.com/${GITHUB_USERNAME}/${REPO_NAME}.git temp_repo
  cd temp_repo
  mvn clean install
  cd target
  NEW_HASH=$(sha256sum $(ls *.jar | grep -v 'original' | head -n 1) | awk '{print $1}')
  cd ../..
  rm -rf temp_repo

  echo "New GitHub JAR hash: $NEW_HASH"

  if [ "$OLD_HASH" != "$NEW_HASH" ]; then
    echo "Hashes are different. Updating application..."

    ssh -o StrictHostKeyChecking=no -i "$KEY_NAME.pem" ec2-user@"$PUBLIC_IP" "
      sudo pkill -f 'java -jar' || true
      cd /home/ec2-user
      rm -rf $REPO_NAME
      git clone https://${GITHUB_USERNAME}:${GITHUB_PAT}@github.com/${GITHUB_USERNAME}/${REPO_NAME}.git
      cd $REPO_NAME
      mvn clean install
      cd target
      JAR_FILE=\$(ls *.jar | grep -v 'original' | head -n 1)
      nohup java -jar \"\$JAR_FILE\" > /home/ec2-user/app.log 2>&1 &
    "

    echo "Application updated successfully."
  else
    echo "No update needed. Application is up-to-date."
  fi

else
  echo "Fresh instance. Skipping hash checking."
fi

echo "Deployment complete."
