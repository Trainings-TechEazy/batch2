package com.techeazy.aws.batch2.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UsersFilesDetailsRecordDTO {


    private String fileName;
    private String userName;
    private LocalDateTime uploadedAt;
//
//    private String documentName;
//    private String privacyType;
//    private Integer retentionAge;

}
