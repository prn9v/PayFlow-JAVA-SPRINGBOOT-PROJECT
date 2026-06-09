// SettlementNotFoundException.java
package com.pranav.wallet_service.exception;

public class SettlementNotFoundException extends RuntimeException {
    public SettlementNotFoundException(String message) { super(message); }
}