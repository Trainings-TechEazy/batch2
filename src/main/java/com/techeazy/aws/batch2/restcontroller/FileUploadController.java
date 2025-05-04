package com.techeazy.aws.batch2.restcontroller;

import java.io.File;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.techeazy.aws.batch2.constant.ErrorCodeEnum;
import com.techeazy.aws.batch2.dto.UsersFilesDetailsRecordDTO;
import com.techeazy.aws.batch2.exception.FileValidationException;
import com.techeazy.aws.batch2.mapper.DtoMapper;
import com.techeazy.aws.batch2.service.S3Service;
import com.techeazy.aws.batch2.service.UsersFilesDetailsManagerService;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@RestController
@RequestMapping("/api/fileupload")
public class FileUploadController {

	private S3Service s3Service;
	
	private UsersFilesDetailsManagerService usersFilesDetailsManagerService;

	public FileUploadController(S3Service s3Service, UsersFilesDetailsManagerService usersFilesDetailsManagerService) {
		this.s3Service = s3Service;
		this.usersFilesDetailsManagerService = usersFilesDetailsManagerService;
	} 
	
	@PostMapping
	public String uploadFile(@RequestPart("file") MultipartFile file, @RequestParam("username") String username)
			throws IOException {

		if (file.isEmpty()) {
			throw new FileValidationException(
					ErrorCodeEnum.EMPTY_FILE.getErrorCode(),
					ErrorCodeEnum.EMPTY_FILE.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}


//		String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
		
		String tempDir = System.getProperty("java.io.tmpdir");
		String filePath = tempDir + username + "-" + file.getOriginalFilename();

		file.transferTo(new File(filePath));

		String fileName = file.getOriginalFilename();

		PutObjectResponse Response = s3Service.uploadToS3(filePath, fileName, username);
		
		UsersFilesDetailsRecordDTO dto = DtoMapper.mapToUsersFilesDetailsRecordDTO(fileName,username);
        
        // Save to DB
        usersFilesDetailsManagerService.saveUploadRecord(dto);
		
		
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
	
}
