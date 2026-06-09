// RefundNotFoundException.java
package com.pranav.payment_service.exception;
public class RefundNotFoundException extends RuntimeException {
    public RefundNotFoundException(String message) { super(message); }
}