package com.techeazy.aws.batch2.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3Service {
	@Value("${aws.access-key-id}")
	private String accessKeyId;

	@Value("${aws.secret-access-key}")
	private String secretAccessKey;

	@Value("${aws.region}")
	private String region;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	public  PutObjectResponse uploadToS3(String localFilePath, String orignalFileName) {

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

		S3Client s3 = S3Client.builder()
				.region(Region.of(region)) // change region if needed
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();

		try {
			PutObjectRequest putOb = PutObjectRequest.builder()
													.bucket(bucketName)
													.key(orignalFileName)
													.build();

			PutObjectResponse response = s3.putObject(putOb, Paths.get(localFilePath));
			System.out.println(" File uploaded successfully!");
			return response;

		} catch (S3Exception e) {
			System.err.println("Upload failed: " + e.awsErrorDetails().errorMessage());
			throw e;
		}finally {
			
			s3.close();
		}

	}
	
	public List<Map<String, String>> listAllFilesWithETags() {
	    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

	    S3Client s3 = S3Client.builder()
	            .region(Region.of(region))
	            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
	            .build();

	    List<Map<String, String>> fileDetails = new ArrayList<>();

	    try {
	        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
	                .bucket(bucketName)
	                .build();  // No prefix

	        ListObjectsV2Response listRes = s3.listObjectsV2(listReq);

	        for (S3Object obj : listRes.contents()) {
	            Map<String, String> fileInfo = new HashMap<>();
	            fileInfo.put("fileName", obj.key());
	            fileInfo.put("eTag", obj.eTag());
	            fileDetails.add(fileInfo);
	        }

	    } catch (S3Exception e) {
	        System.err.println("Error fetching files: " + e.awsErrorDetails().errorMessage());
	    }

	    s3.close();
	    return fileDetails;
	}

}
