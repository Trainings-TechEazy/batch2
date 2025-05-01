package com.techeazy.aws.batch2.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.techeazy.aws.batch2.helper.S3ClientProvider;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
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
    
	public  PutObjectResponse uploadToS3(String localFilePath, String orignalFileName) {

        S3Client s3 = s3ClientProvider.getClient();

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

		S3Client s3 = s3ClientProvider.getClient();

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
	
	public byte[] downloadFileFromS3(String fileName) {

        S3Client s3 = s3ClientProvider.getClient();

	    try {
	        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
	                .bucket(bucketName)
	                .key(fileName)  // here, fileName is the object's key
	                .build();

	        // Fetch the object from S3
	        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(getObjectRequest);

	        return objectBytes.asByteArray();  // Return the file content as byte array

	    } catch (S3Exception e) {
	        System.err.println("Download failed: " + e.awsErrorDetails().errorMessage());
	        throw e;
	    } finally {
	        s3.close();
	    }
	}


}
