package com.techeazy.aws.batch2.exception;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class S3OperationException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8714076589594966025L;
	private final int errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public S3OperationException(int errorCode, String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}

