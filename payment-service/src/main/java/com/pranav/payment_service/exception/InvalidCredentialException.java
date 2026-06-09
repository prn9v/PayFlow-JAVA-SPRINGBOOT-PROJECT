// InvalidCredentialException.java
package com.pranav.payment_service.exception;
public class InvalidCredentialException extends RuntimeException {
    public InvalidCredentialException(String message) { super(message); }
}