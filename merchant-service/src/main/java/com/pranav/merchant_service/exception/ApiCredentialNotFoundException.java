// ApiCredentialNotFoundException.java
package com.pranav.merchant_service.exception;

public class ApiCredentialNotFoundException extends RuntimeException {
    public ApiCredentialNotFoundException(String message) {
        super(message);
    }
}