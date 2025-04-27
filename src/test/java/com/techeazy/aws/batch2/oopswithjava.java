import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

import software.amazon.awssdk.auth.credentials. StaticCredentialsProvider:

import software.amazon.awssdk.regions.Region:

import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import software.amazon.awssdk.services.s3.model.S3Exception:

public class 53Service {

public static void main(String)] args){

String accesskey = "YOUR ACCESS_KEY":

String secretKey = "YOUR SECRET_KEY":

String bucketName = "your-bucket-name";

String key = "sample.txt";

String filePath = "sample.txt"; // make sure this file exists locally

AwsBasicCredentials awsCreds AwsBasicCredentials.create(accessKey, SecretKey);

S3Client 53 = $3Client.builder().region(Region.US EAST 1) // change region if needed

.credentialsProvider ( StaticCredentialsProvider.createLawscreds)).build();

try {

PutObjectRequest putob PutObjectRequest.builder().bucket(bucketNamg),key(key).build():

s3.putObject(putok, Paths.get(filePath));

System.out.println(" File uploaded successfully!");

} catch (Exception e) {

System.err.println("X Upload failed: "+e.awaEccorDetails().errorMessage());

}

s3.closel(); 
}