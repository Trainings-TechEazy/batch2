package com.techeazy.aws.batch2.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.techeazy.aws.batch2.constant.ErrorCodeEnum;
import com.techeazy.aws.batch2.dto.UsersFilesDetailsRecordDTO;
import com.techeazy.aws.batch2.entity.UsersFilesDetailsRecordEntity;
import com.techeazy.aws.batch2.exception.DatabaseOperationException;
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
		
		 try {
	            repository.save(usersFilesDetailsRecordEntity);
	        } catch (Exception e) {
	            throw new DatabaseOperationException(
	            		ErrorCodeEnum.UPLOAD_RECORD_TO_DB_FAILED.getErrorCode(),
	            		ErrorCodeEnum.UPLOAD_RECORD_TO_DB_FAILED.getErrorMessage(),
	            		HttpStatus.INTERNAL_SERVER_ERROR);
	        }

	}

	public List<String> getFileNamesByUser(String userName) {
		 try {
	            return repository.findFileNamesByUserName(userName);
	        } catch (Exception e) {
	        	throw new DatabaseOperationException(
	            		ErrorCodeEnum.USERS_FILE_RECORD_FETCH_FROM_DB_FAILED.getErrorCode(),
	            		ErrorCodeEnum.USERS_FILE_RECORD_FETCH_FROM_DB_FAILED.getErrorMessage(),
	            		HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	}

	public List<UsersFilesDetailsRecordDTO> getAllUploadRecords() {
		 try {
	            List<UsersFilesDetailsRecordEntity> entities = repository.findAll();

	            return entities.stream().map(entity -> {
	                UsersFilesDetailsRecordDTO dto = new UsersFilesDetailsRecordDTO();
	                dto.setFileName(entity.getFileName());
	                dto.setUserName(entity.getUserName());
	                dto.setUploadedAt(entity.getUploadedAt());
	                return dto;
	            }).collect(Collectors.toList());

	        } catch (Exception e) {
	        	throw new DatabaseOperationException(
	            		ErrorCodeEnum.FETCH_ALL_USERS_RECORDS_FROM_DB_FAILED.getErrorCode(),
	            		ErrorCodeEnum.FETCH_ALL_USERS_RECORDS_FROM_DB_FAILED.getErrorMessage(),
	            		HttpStatus.INTERNAL_SERVER_ERROR);	        
	        }	
	}		 

}
	
	
	
	