package com.techeazy.aws.batch2.constant;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ErrorCodeEnum {
	
    EMPTY_FILE(1001, "File sent is empty!"),
    
	UPLOAD_FAILED(2001,"Error while file uploading into s3"),
	DOWNLOAD_FAILED(2002,"Error while downloading file from s3"),
	FILE_LIST_FETCHING_FAILED(2003,"Error while fetching file list from s3"),
	
	UPLOAD_RECORD_TO_DB_FAILED(3001,"Error while uploading records into DB"),
	USERS_FILE_RECORD_FETCH_FROM_DB_FAILED(3002,"Error while fetching users file record from DB"),
	FETCH_ALL_USERS_RECORDS_FROM_DB_FAILED(3003,"Error while fetching all users all data from DB");
	

    private final int errorCode;
    private final String errorMessage;

    ErrorCodeEnum(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}

