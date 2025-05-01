package com.techeazy.aws.batch2.restcontroller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techeazy.aws.batch2.service.S3Service;

@RestController
@RequestMapping("/api/filedownload")
public class FileDownloadController {

	private S3Service s3Service;
	
	public FileDownloadController(S3Service s3Service) {
		super();
		this.s3Service = s3Service;
	}
	
	//fetch file from s3 bucket	
		@GetMapping("/download")
		public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {
		    byte[] fileData = s3Service.downloadFileFromS3(fileName);

		    // Wrap byte[] into a Resource
		    ByteArrayResource resource = new ByteArrayResource(fileData);

		    return ResponseEntity.ok()
		            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
		            .contentType(MediaType.APPLICATION_OCTET_STREAM)
		            .contentLength(fileData.length)
		            .body(resource);
		}
}
