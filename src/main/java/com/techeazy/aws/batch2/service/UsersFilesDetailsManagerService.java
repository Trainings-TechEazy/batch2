package com.techeazy.aws.batch2.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techeazy.aws.batch2.dto.UsersFilesDetailsRecordDTO;
import com.techeazy.aws.batch2.entity.UsersFilesDetailsRecordEntity;
import com.techeazy.aws.batch2.repository.UsersFilesDetailsRecordRepository;

@Service
public class UsersFilesDetailsManagerService {

    @Autowired
    private UsersFilesDetailsRecordRepository repository;
	
    public void saveUploadRecord(UsersFilesDetailsRecordDTO dto) {

    	UsersFilesDetailsRecordEntity usersFilesDetailsRecordEntity = new UsersFilesDetailsRecordEntity();    	
    	usersFilesDetailsRecordEntity.setFileName(dto.getFileName());
    	usersFilesDetailsRecordEntity.setUserName(dto.getUserName());
    	usersFilesDetailsRecordEntity.setUploadedAt(dto.getUploadedAt());

        repository.save(usersFilesDetailsRecordEntity);
	
    }
    public List<String> getFileNamesByUser(String userName) {
        return repository.findFileNamesByUserName(userName);
    }
    
    public List<UsersFilesDetailsRecordDTO> getAllUploadRecords() {
        List<UsersFilesDetailsRecordEntity> entities = repository.findAll();

        return entities.stream()
        		.map(entity -> {
		            UsersFilesDetailsRecordDTO dto = new UsersFilesDetailsRecordDTO();
		            dto.setFileName(entity.getFileName());
		            dto.setUserName(entity.getUserName());
		            dto.setUploadedAt(entity.getUploadedAt());
		            return dto;
        			})
        		.collect(Collectors.toList());
    }

    
    
}