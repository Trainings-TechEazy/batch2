package com.techeazy.aws.batch2.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FileValidationException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5932836349805332572L;  
	private final int errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public FileValidationException(int errorCode, String errorMessage, HttpStatus httpStatus) {
        super(errorMessage); // optional but useful for stack trace and logging
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}
