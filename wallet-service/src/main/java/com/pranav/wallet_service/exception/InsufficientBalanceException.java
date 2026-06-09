// InsufficientBalanceException.java
package com.pranav.wallet_service.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) { super(message); }
}