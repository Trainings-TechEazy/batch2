package com.techeazy.aws.batch2.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.techeazy.aws.batch2.constant.ErrorCodeEnum;
import com.techeazy.aws.batch2.exception.S3OperationException;
import com.techeazy.aws.batch2.helper.S3ClientProvider;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3Service {

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	private final S3ClientProvider s3ClientProvider;

	public S3Service(S3ClientProvider s3ClientProvider) {
		super();
		this.s3ClientProvider = s3ClientProvider;
	}

	// upload object to s3
	public PutObjectResponse uploadToS3(String localFilePath, String orignalFileName, String username) {

		S3Client s3 = s3ClientProvider.getClient();
		String uniqueKeyForFile = username + "/" + orignalFileName;

		try {
			PutObjectRequest putOb = PutObjectRequest
										.builder()
										.bucket(bucketName)
										.key(uniqueKeyForFile)
										.build();

			PutObjectResponse response = s3.putObject(putOb, Paths.get(localFilePath));
			System.out.println(" File uploaded successfully!");
			return response;

		} catch (S3Exception e) {
			System.err.println("Upload failed: " + e.awsErrorDetails().errorMessage());
			throw new S3OperationException(
					ErrorCodeEnum.UPLOAD_FAILED.getErrorCode(),
					ErrorCodeEnum.UPLOAD_FAILED.getErrorMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {

			s3.close();
		}

	}

	// download object from s3
	public byte[] downloadFileFromS3(String fileName, String username) {

		S3Client s3 = s3ClientProvider.getClient();

		String uniqueKeyForFile = username + "/" + fileName;

		try {
			GetObjectRequest getObjectRequest = GetObjectRequest
												.builder()
												.bucket(bucketName)
												.key(uniqueKeyForFile)
												.build();

			// Fetch the object from S3
			ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(getObjectRequest);

			return objectBytes.asByteArray();

		} catch (S3Exception e) {
			System.err.println("Download failed: " + e.awsErrorDetails().errorMessage());
			throw new S3OperationException(
					ErrorCodeEnum.DOWNLOAD_FAILED.getErrorCode(),
					ErrorCodeEnum.DOWNLOAD_FAILED.getErrorMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			s3.close();
		}
	}

	// Get List of All Objects of user from bucket
	public List<String> listUserObjects(String username) {
		String prefix = username + "/";
		S3Client s3= s3ClientProvider.getClient();

		try {
			ListObjectsV2Request listRequest = ListObjectsV2Request
												.builder()
												.bucket(bucketName)
												.prefix(prefix)
												.build();


			ListObjectsV2Response response = s3.listObjectsV2(listRequest);

			List<String> objectKeys = new ArrayList<>();
			for (S3Object object : response.contents()) {
				objectKeys.add(object.key());
			}

			return objectKeys;

		} catch (S3Exception e) {
			System.err.println("Fetching file list from s3 failed: " + e.awsErrorDetails().errorMessage());
			throw new S3OperationException(
					ErrorCodeEnum.FILE_LIST_FETCHING_FAILED.getErrorCode(),
					ErrorCodeEnum.FILE_LIST_FETCHING_FAILED.getErrorMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			s3.close();
		}
	}

}
