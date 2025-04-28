package com.techeazy.aws.batch2.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
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
            System.out.println("File uploaded successfully!");

        } catch (S3Exception e) {
            System.err.println("Upload failed: " + e.awsErrorDetails().errorMessage());
        } finally {
            s3.close();
        }
    }

    public List<String> listFilesForUser(String username, List<String> filenames) {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        List<String> fileNames = new ArrayList<>();

        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(username + "/") // Important: Only that user's files
                    .build();

            ListObjectsV2Response listRes = s3.listObjectsV2(listReq);

            for (S3Object obj : listRes.contents()) {
                fileNames.add(obj.key());
            }

            if (fileNames.isEmpty()) {
                return List.of("No files found for user: " + username);
            }

            if (filenames != null && !filenames.isEmpty()) {
                List<String> filteredFiles = fileNames.stream()
                        .filter(file -> filenames.contains(Paths.get(file).getFileName().toString()))
                        .collect(Collectors.toList());

                if (filteredFiles.isEmpty()) {
                    return List.of("No matching files found for provided list.");
                }

                return filteredFiles;
            } else {
                return fileNames;
            }

        } catch (S3Exception e) {
            System.err.println("Listing files failed: " + e.awsErrorDetails().errorMessage());
            return List.of("Error while listing files: " + e.awsErrorDetails().errorMessage());
        } finally {
            s3.close();
        }
    }
}
