package com.techeazy.aws.batch2.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.techeazy.aws.batch2.pojo.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleFileValidationException(FileValidationException ex) {
		
		log.info("handleFileValidationException called");		
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getErrorMessage());
		log.info(errorResponse.toString());
		return new ResponseEntity<>(errorResponse,ex.getHttpStatus());
	}
	
	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleDatabaseOperationException(DatabaseOperationException ex) {
		
		log.info("handleDatabaseOperationException called");		
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getErrorMessage());
		log.info(errorResponse.toString());
		return new ResponseEntity<>(errorResponse,ex.getHttpStatus());
	}
	
	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleS3OperationException(S3OperationException ex) {
		
		log.info("handleS3OperationException called");		
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getErrorMessage());
		log.info(errorResponse.toString());
		return new ResponseEntity<>(errorResponse,ex.getHttpStatus());
	}
	
}
