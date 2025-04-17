package java.com.techeazy.aws.batch2.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/files")
public class S3ListFilesController {

    @GetMapping
    public List<String> listFiles(@RequestParam String userName) {
        // Placeholder logic - replace with actual S3 call in future
        return Arrays.asList("file1.txt", "file2.jpg", "report_" + userName + ".pdf");
    }
}
