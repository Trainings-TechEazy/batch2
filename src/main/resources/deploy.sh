#!/bin/bash

set -e

# === CONFIGURATION ===
KEY_NAME="springboot-key"
SECURITY_GROUP_NAME="springboot-sg"
INSTANCE_NAME="springboot-instance"
REGION="us-east-1"
AMI_ID="ami-0e449927258d45bc4"  # Amazon Linux 2
INSTANCE_TYPE="t2.micro"
LOG_GROUP_NAME="ec2-springboot"
GITHUB_USERNAME="Bablu7011"
#GITHUB_PAT direct export through environment variable
REPO_NAME="Springbootproject"

# === CLEANUP (Optional) ===
echo "🧹 Cleaning up old resources..."
aws ec2 delete-key-pair --key-name $KEY_NAME || true
aws logs delete-log-group --log-group-name $LOG_GROUP_NAME || true
aws ec2 delete-security-group --group-name $SECURITY_GROUP_NAME || true
rm -f $KEY_NAME.pem

# === CREATE KEY PAIR ===
echo "🔐 Creating key pair: $KEY_NAME"
aws ec2 create-key-pair --key-name $KEY_NAME --query 'KeyMaterial' --output text > $KEY_NAME.pem
chmod 400 $KEY_NAME.pem

# === CREATE SECURITY GROUP ===
echo "🛡️ Creating security group: $SECURITY_GROUP_NAME"
SG_ID=$(aws ec2 create-security-group \
  --group-name $SECURITY_GROUP_NAME \
  --description "Security group for Spring Boot app" \
  --query 'GroupId' --output text)

# Authorize ports
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 22 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 80 --cidr 0.0.0.0/0

# === CREATE LOG GROUP ===
echo "📊 Creating CloudWatch log group: $LOG_GROUP_NAME"
aws logs create-log-group --log-group-name $LOG_GROUP_NAME 2>/dev/null || echo "ℹ️ Log group may already exist"

# === USER DATA SCRIPT ===
echo "📄 Writing user-data script..."
USER_DATA=$(cat <<EOF
#!/bin/bash
sudo yum update -y

# Install Java 17
sudo yum install java-17-amazon-corretto-devel -y

# Install Git
sudo yum install git -y

# Install Maven
cd /opt
sudo curl -O https://archive.apache.org/dist/maven/maven-3/3.9.1/binaries/apache-maven-3.9.1-bin.tar.gz
sudo tar -xvzf apache-maven-3.9.1-bin.tar.gz
echo "export M2_HOME=/opt/apache-maven-3.9.1" | sudo tee -a /etc/profile
echo "export PATH=\$M2_HOME/bin:\$PATH" | sudo tee -a /etc/profile
source /etc/profile

# Clone repo
cd /home/ec2-user
git clone https://${GITHUB_USERNAME}:${GITHUB_PAT}@github.com/${GITHUB_USERNAME}/${REPO_NAME}.git

# Build the project
cd ${REPO_NAME}
mvn clean install

# Run the JAR
cd target
JAR_FILE=\$(ls *.jar | grep -v 'original' | head -n 1)
nohup java -jar "\$JAR_FILE" > /home/ec2-user/app.log 2>&1 &

# Install CloudWatch agent
sudo yum install -y amazon-cloudwatch-agent

# CloudWatch agent config
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

# Start CloudWatch agent
/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 -c file:/opt/cloudwatch-config.json -s
EOF
)

# === LAUNCH EC2 INSTANCE ===
echo "🚀 Launching EC2 instance..."
INSTANCE_ID=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --instance-type $INSTANCE_TYPE \
  --key-name $KEY_NAME \
  --security-group-ids $SG_ID \
  --user-data "$USER_DATA" \
  --region $REGION \
  --query 'Instances[0].InstanceId' \
  --output text)

# === WAIT FOR INSTANCE ===
echo "⏳ Waiting for instance to be running..."
aws ec2 wait instance-running --instance-ids $INSTANCE_ID

# === GET PUBLIC IP ===
PUBLIC_IP=$(aws ec2 describe-instances \
  --instance-ids $INSTANCE_ID \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text)

echo "✅ Instance is live!"
echo "🌍 Visit: http://$PUBLIC_IP:80"
echo "📄 CloudWatch Logs: https://$REGION.console.aws.amazon.com/cloudwatch/home?region=$REGION#logsV2:log-groups/log-group/$LOG_GROUP_NAME"
