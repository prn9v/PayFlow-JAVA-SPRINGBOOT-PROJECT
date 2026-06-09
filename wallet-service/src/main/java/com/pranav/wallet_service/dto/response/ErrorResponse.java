// ErrorResponse.java
package com.pranav.wallet_service.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int    status;
    private String message;
}