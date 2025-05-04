package com.techeazy.aws.batch2.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.techeazy.aws.batch2.dto.UsersFilesDetailsRecordDTO;

@Component
public class DtoMapper {

	public static UsersFilesDetailsRecordDTO mapToUsersFilesDetailsRecordDTO(String fileName,
			String userName) {
		UsersFilesDetailsRecordDTO dto = new UsersFilesDetailsRecordDTO();
		dto.setFileName(fileName);
		dto.setUserName(userName);
		dto.setUploadedAt(LocalDateTime.now());
		return dto;
	}

}
