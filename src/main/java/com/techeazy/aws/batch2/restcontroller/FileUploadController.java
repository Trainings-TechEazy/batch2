package com.techeazy.aws.batch2.restcontroller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.techeazy.aws.batch2.dto.FileUploadRecordDTO;
import com.techeazy.aws.batch2.service.FileUploadRecordService;
import com.techeazy.aws.batch2.service.S3Service;

import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@RestController
@RequestMapping("/api/fileupload")
public class FileUploadController {

	@Autowired
	private S3Service s3Service;
	
	@Autowired
	private FileUploadRecordService fileUploadRecordService;

	@PostMapping
	public String uploadFile(@RequestPart("file") MultipartFile file, @RequestParam("username") String username)
			throws IOException {

		if (file.isEmpty()) {
			return "Upload failed: file is empty.";
		}

// filePath is not working in Windows OS 		
// Hence making changes in filePath code	
// Linux-style path "/tmp/", which will not work in Windows OS, where /tmp/ doesn't exist	
		
// Get Temp Dir path, storing it in String 		
		String tempDir = System.getProperty("java.io.tmpdir");
//		System.out.println(tempDir);
		
//		String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
		String filePath = tempDir + username + "-" + file.getOriginalFilename();
//		System.out.println(filePath);

		file.transferTo(new File(filePath));

		// Uploading to S3 and getting eTag
		PutObjectResponse eTagResponse = s3Service.uploadToS3(filePath, file.getOriginalFilename());
		
		
		// Prepare DTO
        FileUploadRecordDTO dto = new FileUploadRecordDTO();
        dto.setFileName(file.getOriginalFilename());
        dto.setETag(eTagResponse.eTag()); // Assuming eTag returned from S3Service
        dto.setUserName(username);
        dto.setUploadedAt(LocalDateTime.now());
        
        // Save to DB
        fileUploadRecordService.saveUploadRecord(dto);
		
		
		return String.format("File uploaded by [%s], saved at %s", username, filePath);
	}

	@PostMapping("/meta")
	public String uploadFileWithMeta(@RequestPart("file") MultipartFile file, @RequestParam("username") String username,
			@RequestParam("documentName") String documentName, @RequestParam("privacyType") String privacyType,
			@RequestParam("retentionAge") int retentionAge) throws IOException {

		if (file.isEmpty()) {
			return "Upload failed: file is empty.";
		}
		
// filePath is not working in Windows OS 		
// Hence making changes in filePath code		
		String tempDir = System.getProperty("java.io.tmpdir");

//		String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
		String filePath = tempDir + username + "-" + file.getOriginalFilename();

		file.transferTo(new File(filePath));

		return String.format("File uploaded by [%s], name: %s, privacy: %s, retention: %d days, saved at %s", username,
				documentName, privacyType, retentionAge, filePath);
	}
}
