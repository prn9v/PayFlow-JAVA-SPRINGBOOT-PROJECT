// OtpCooldownException.java
package com.pranav.auth_service.exception;

public class OtpCooldownException extends RuntimeException {
    public OtpCooldownException(String message) {
        super(message);
    }
}