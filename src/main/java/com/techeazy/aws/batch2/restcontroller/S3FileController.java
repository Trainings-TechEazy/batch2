package com.techeazy.aws.batch2.restcontroller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/files")
public class S3FileController {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @GetMapping("/{userName}")
    public ResponseEntity<List<String>> listFiles(@PathVariable String userName) {
        String prefix = "users/" + userName + "/"; // e.g., users/john/

        ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix);

        ListObjectsV2Result result = amazonS3.listObjectsV2(req);

        List<String> filenames = new ArrayList<>();
        for (S3ObjectSummary summary : result.getObjectSummaries()) {
            String key = summary.getKey();
            String fileName = key.replace(prefix, ""); // Remove the user folder path
            if (!fileName.isEmpty()) {
                filenames.add(fileName);
            }
        }

        return ResponseEntity.ok(filenames);
    }
}