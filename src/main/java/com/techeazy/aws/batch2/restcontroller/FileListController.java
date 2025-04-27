package com.techeazy.aws.batch2.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.techeazy.aws.batch2.service.S3Service;

@RestController
@RequestMapping("/api/files")
public class FileListController {

    @Autowired
    private S3Service s3Service;

    @GetMapping
    public List<String> listUserFiles(@RequestParam String username) {
        return s3Service.listFilesForUser(username);
    }
}