package com.techeazy.aws.batch2.restcontroller;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.techeazy.aws.batch2.service.S3Service;

@RestController
@RequestMapping("/api/fileupload")
public class FileUploadController {

    @Autowired
    private S3Service s3Service;

    @PostMapping
    public String uploadFile(@RequestPart("file") MultipartFile file, @RequestParam("username") String username)
            throws IOException {

        if (file.isEmpty()) {
            return "Upload failed: file is empty.";
        }

        String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
        file.transferTo(new File(filePath));

        s3Service.uploadToS3(filePath, username + "/" + file.getOriginalFilename()); // Save with username prefix

        return String.format("File uploaded by [%s], saved at %s", username, filePath);
    }

    @PostMapping("/meta")
    public String uploadFileWithMeta(
            @RequestPart("file") MultipartFile file,
            @RequestParam("username") String username,
            @RequestParam("documentName") String documentName,
            @RequestParam("privacyType") String privacyType,
            @RequestParam("retentionAge") int retentionAge) throws IOException {

        if (file.isEmpty()) {
            return "Upload failed: file is empty.";
        }

        String filePath = "/tmp/" + username + "-" + file.getOriginalFilename();
        file.transferTo(new File(filePath));

        s3Service.uploadToS3(filePath, username + "/" + documentName); // Save with documentName

        return String.format("File uploaded by [%s], name: %s, privacy: %s, retention: %d days, saved at %s",
                username, documentName, privacyType, retentionAge, filePath);
    }
}
