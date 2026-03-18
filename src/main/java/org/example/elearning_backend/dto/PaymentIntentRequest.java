package org.example.elearning_backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class PaymentIntentRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private String currency = "usd"; // Default currency

    private String paymentMethodId; // Optional, for automatic confirmation
}