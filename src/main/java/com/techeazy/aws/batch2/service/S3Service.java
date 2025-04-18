package com.techeazy.aws.batch2.service;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
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

	public void uploadToS3(String localFilePath, String key) {

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

		S3Client s3 = S3Client.builder().region(Region.of(region)) // change region if needed
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds)).build();

		try {
			PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(key).build();

			s3.putObject(putOb, Paths.get(localFilePath));
			System.out.println(" File uploaded successfully!");

		} catch (S3Exception e) {
			System.err.println("Upload failed: " + e.awsErrorDetails().errorMessage());
		}

		s3.close();
	}

	
	//Method method to find findALl Files
	private final S3Client s3Client;
	
	public S3Service() {
		this.s3Client = S3Client.builder().region(Region.US_EAST_1) // Replace with your bucket's region
				.credentialsProvider(ProfileCredentialsProvider.create()).build();
	}

	
	public List<String> listFiles(String userName) {
		ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).prefix(userName + "/").build();

		ListObjectsV2Response result = s3Client.listObjectsV2(request);

		return result.contents().stream().map(S3Object::key).collect(Collectors.toList());
	}
}
