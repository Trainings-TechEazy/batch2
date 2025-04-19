package com.techeazy.aws.batch2.restcontroller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.techeazy.aws.batch2.service.S3Service;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

@RestController
@RequestMapping("/api/fileupload")
public class FileUploadController {

	@Autowired
	private S3Service s3Service;

	private final S3Client s3Client = S3Client.builder()
			.region(Region.AP_SOUTH_1) // Change region as needed
			.credentialsProvider(ProfileCredentialsProvider.create())
			.build();

	private final String bucketName = "your-bucket-name"; // <-- Replace with your actual bucket name

	@PostMapping
	public String uploadFile(@RequestPart("file") MultipartFile file, @RequestParam("username") String username)
			throws IOException {

		if (file.isEmpty()) {
			return "Upload failed: file is empty.";
		}

		String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
		file.transferTo(new File(filePath));

		s3Service.uploadToS3(filePath, file.getOriginalFilename());

		return String.format("File uploaded by [%s], saved at %s", username, filePath);
	}

	@PostMapping("/meta")
	public String uploadFileWithMeta(@RequestPart("file") MultipartFile file, @RequestParam("username") String username,
									 @RequestParam("documentName") String documentName, @RequestParam("privacyType") String privacyType,
									 @RequestParam("retentionAge") int retentionAge) throws IOException {

		if (file.isEmpty()) {
			return "Upload failed: file is empty.";
		}

		String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
		file.transferTo(new File(filePath));

		return String.format("File uploaded by [%s], name: %s, privacy: %s, retention: %d days, saved at %s", username,
				documentName, privacyType, retentionAge, filePath);
	}

	@GetMapping("/list")
	public List<String> listFilesForUser(@RequestParam String userName) {
		return s3Service.listFilesForUser(userName);
	}

}
