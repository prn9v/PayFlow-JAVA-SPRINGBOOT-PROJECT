// PaymentNotFoundException.java
package com.pranav.payment_service.exception;
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) { super(message); }
}