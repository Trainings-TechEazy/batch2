package com.techeazy.aws.batch2.service;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
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

		S3Client s3 = S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();

		try {
			PutObjectRequest putOb = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(key)
					.build();

			s3.putObject(putOb, Paths.get(localFilePath));
			System.out.println(" File uploaded successfully!");

		} catch (S3Exception e) {
			System.err.println("Upload failed: " + e.awsErrorDetails().errorMessage());
		}

		s3.close();
	}

	public List<String> listFilesForUser(String userName) {

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

		S3Client s3 = S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();

		List<String> fileNames = null;

		try {
			ListObjectsV2Request request = ListObjectsV2Request.builder()
					.bucket(bucketName)
					.prefix(userName + "/")
					.build();

			fileNames = s3.listObjectsV2(request)
					.contents()
					.stream()
					.map(S3Object::key)
					.collect(Collectors.toList());

		} catch (S3Exception e) {
			System.err.println("List failed: " + e.awsErrorDetails().errorMessage());
		}

		s3.close();

		return fileNames;
	}
}
