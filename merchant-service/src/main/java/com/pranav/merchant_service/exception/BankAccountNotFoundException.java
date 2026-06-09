// BankAccountNotFoundException.java
package com.pranav.merchant_service.exception;

public class BankAccountNotFoundException extends RuntimeException {
    public BankAccountNotFoundException(String message) {
        super(message);
    }
}