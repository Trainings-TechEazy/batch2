package com.techeazy.aws.batch2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techeazy.aws.batch2.dto.FileUploadRecordDTO;
import com.techeazy.aws.batch2.entity.FileUploadRecordEntity;
import com.techeazy.aws.batch2.repository.FileUploadRecordRepository;

@Service
public class FileUploadRecordService {

    @Autowired
    private FileUploadRecordRepository repository;
	
    public void saveUploadRecord(FileUploadRecordDTO dto) {

    	FileUploadRecordEntity fileUploadRecordEntity = new FileUploadRecordEntity();    	
        fileUploadRecordEntity.setFileName(dto.getFileName());
        fileUploadRecordEntity.setETag(dto.getETag());
        fileUploadRecordEntity.setUserName(dto.getUserName());
        fileUploadRecordEntity.setUploadedAt(dto.getUploadedAt());

        repository.save(fileUploadRecordEntity);
	
    }
}