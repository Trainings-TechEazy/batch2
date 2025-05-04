package com.techeazy.aws.batch2.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techeazy.aws.batch2.dto.UsersFilesDetailsRecordDTO;
import com.techeazy.aws.batch2.service.S3Service;
import com.techeazy.aws.batch2.service.UsersFilesDetailsManagerService;

@RestController
@RequestMapping("/api/user-details")
public class UsersFilesDetailsController {
	
	private UsersFilesDetailsManagerService usersFilesDetailsManagerService;
	
	private S3Service s3Service;

	public UsersFilesDetailsController(UsersFilesDetailsManagerService usersFilesDetailsManagerService,S3Service s3Service) {
		super();
		this.usersFilesDetailsManagerService = usersFilesDetailsManagerService;
		this.s3Service = s3Service;
	}

	//fetch file names from s3 bucket	
	 @GetMapping("/user-files/{username}")
	    public List<String> listUserFiles(@PathVariable String username) {
	        return s3Service.listUserObjects(username);
	    }	
	
	//Fetch users all files list to show him his files details from DB     
		@GetMapping("/user-files")
		public ResponseEntity<List<String>> getFilesByUser(@RequestParam("username") String username) {
		    List<String> fileNames = usersFilesDetailsManagerService.getFileNamesByUser(username);
		    if (fileNames.isEmpty()) {
		        return ResponseEntity.noContent().build();
		    }
		    return ResponseEntity.ok(fileNames);
		}
 
	//fetch all files list stored from DB	
//		@GetMapping("/all-files")
//		public ResponseEntity<List<Map<String, String>>> listAllFilesWithUsernames() {
//		    List<UsersFilesDetailsRecordDTO> records = usersFilesDetailsManagerService.getAllUploadRecords();
//
//		    if (records.isEmpty()) {
//		        return ResponseEntity.noContent().build();
//		    }
//
//		    List<Map<String, String>> response = records.stream().map(record -> {
//		        Map<String, String> map = new HashMap<>();
//		        map.put("fileName", record.getFileName());
//		        map.put("userName", record.getUserName());
//		        map.put("uploadedAt", record.getUploadedAt().toString());
//		        return map;
//		    }).collect(Collectors.toList());
//
//		    return ResponseEntity.ok(response);
//		}
		
		@GetMapping("/all-files")
		public ResponseEntity<List<UsersFilesDetailsRecordDTO>> listAllFilesWithUsernames() {
		    List<UsersFilesDetailsRecordDTO> records = usersFilesDetailsManagerService.getAllUploadRecords();

		    if (records.isEmpty()) {
		        return ResponseEntity.noContent().build();
		    }

		    return ResponseEntity.ok(records);
		}


}
