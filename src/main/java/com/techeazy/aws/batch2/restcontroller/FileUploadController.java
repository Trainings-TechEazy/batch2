package com.techeazy.aws.batch2.restcontroller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

	private S3Service s3Service;
	
	private FileUploadRecordService fileUploadRecordService;

	public FileUploadController(S3Service s3Service, FileUploadRecordService fileUploadRecordService) {
		this.s3Service = s3Service;
		this.fileUploadRecordService = fileUploadRecordService;
	} 
	
	@PostMapping
	public String uploadFile(@RequestPart("file") MultipartFile file, @RequestParam("username") String username)
			throws IOException {

		if (file.isEmpty()) {
			return "Upload failed: file is empty.";
		}


//		String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
		
		String tempDir = System.getProperty("java.io.tmpdir");
		String filePath = tempDir + username + "-" + file.getOriginalFilename();

		file.transferTo(new File(filePath));

		// Uploading to S3 and getting eTag
		PutObjectResponse Response = s3Service.uploadToS3(filePath, file.getOriginalFilename(),username);
		
		
		// Prepare DTO
        FileUploadRecordDTO dto = new FileUploadRecordDTO();
        dto.setFileName(file.getOriginalFilename());
        dto.setETag(Response.eTag()); // Assuming eTag returned from S3Service
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
			
		String tempDir = System.getProperty("java.io.tmpdir");

//		String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
		String filePath = tempDir + username + "-" + file.getOriginalFilename();

		file.transferTo(new File(filePath));

		return String.format("File uploaded by [%s], name: %s, privacy: %s, retention: %d days, saved at %s", username,
				documentName, privacyType, retentionAge, filePath);
	}
	
	
//Fetch users all files list to show him his files details
	@GetMapping("/user-files")
	public ResponseEntity<List<String>> getFilesByUser(@RequestParam("username") String username) {
	    List<String> fileNames = fileUploadRecordService.getFileNamesByUser(username);
	    if (fileNames.isEmpty()) {
	        return ResponseEntity.noContent().build();
	    }
	    return ResponseEntity.ok(fileNames);
	}

//fetch all files list stored in s3 bucket with user names	
	@GetMapping("/all-files")
	public ResponseEntity<List<Map<String, String>>> listAllFilesWithUsernames() {
	    List<FileUploadRecordDTO> records = fileUploadRecordService.getAllUploadRecords();

	    if (records.isEmpty()) {
	        return ResponseEntity.noContent().build();
	    }

	    List<Map<String, String>> response = records.stream().map(record -> {
	        Map<String, String> map = new HashMap<>();
	        map.put("fileName", record.getFileName());
	        map.put("userName", record.getUserName());
	        map.put("eTag", record.getETag());
	        map.put("uploadedAt", record.getUploadedAt().toString());
	        return map;
	    }).collect(Collectors.toList());

	    return ResponseEntity.ok(response);
	}

	
}
