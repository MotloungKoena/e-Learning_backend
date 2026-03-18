package org.example.elearning_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentResponse {
    private String clientSecret;  // For frontend to confirm payment
    private String paymentIntentId;
    private Long amount;
    private String currency;
    private String status;
}