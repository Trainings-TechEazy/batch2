package com.managedoc.service;

import java.nio.file.Paths;

public class S3service {

public static void main(String[] args) {

String accesskey "YOUR ACCESS_KEY":

String secretkey = "YOUR SECRET_KEY":

String bucketName = "your-bucket-name";

String key="sample.txt";

String filePath = "sample.txt"; // make sure this file exists locolly

AwsBasicCredentials awsCreds AwsBasibcredentials.create(accesskey, secretkey):

S3Client $3 $aClient.builder().region(Region US EAST 11 // change region if needed

credentialsProvider(StaticcredentialsProvider.create(nusCreas)).build();

try {

PutObjectRequest putob PutobjectRequest builder().bucket(bucketName).Key(key).build():

saputobiect(puton, Paths.get(filePath));

System.out.println("☑ File uploaded successfully!");

} catch (S3Exception e) {

System.err.println("X Upload failed:"e.awat.rrorDetails().errorMessage());   } s3.close();
}

}