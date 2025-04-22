package com.techeazy.aws.batch2.service;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<String> getFileNamesByUser(String userName) {
        return repository.findFileNamesByUserName(userName);
    }
    
    public List<FileUploadRecordDTO> getAllUploadRecords() {
        List<FileUploadRecordEntity> entities = repository.findAll();

        return entities.stream()
        		.map(entity -> {
		            FileUploadRecordDTO dto = new FileUploadRecordDTO();
		            dto.setFileName(entity.getFileName());
		            dto.setETag(entity.getETag());
		            dto.setUserName(entity.getUserName());
		            dto.setUploadedAt(entity.getUploadedAt());
		            return dto;
        			})
        		.collect(Collectors.toList());
    }

    
    
}