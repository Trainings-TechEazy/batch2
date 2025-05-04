package com.techeazy.aws.batch2.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DatabaseOperationException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1202723284961598989L;
	private final int errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public DatabaseOperationException(int errorCode, String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}

