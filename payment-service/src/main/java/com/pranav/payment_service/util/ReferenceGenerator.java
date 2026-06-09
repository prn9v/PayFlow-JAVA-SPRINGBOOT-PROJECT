// ReferenceGenerator.java
package com.pranav.payment_service.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ReferenceGenerator {

    private static final AtomicLong counter = new AtomicLong(0);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyMMddHHmmss");

    // Generates: PAY-240601123045-00001
    public String generatePaymentReference() {
        return "PAY-" +
                LocalDateTime.now().format(FMT) + "-" +
                String.format("%05d", counter.incrementAndGet() % 100_000);
    }

    // Generates: REF-240601123045-00001
    public String generateRefundReference() {
        return "REF-" +
                LocalDateTime.now().format(FMT) + "-" +
                String.format("%05d", counter.incrementAndGet() % 100_000);
    }
}