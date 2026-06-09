// InvalidOtpException.java
package com.pranav.auth_service.exception;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }
}