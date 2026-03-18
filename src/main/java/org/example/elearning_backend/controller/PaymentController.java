package org.example.elearning_backend.controller;

import com.stripe.exception.StripeException;
import org.example.elearning_backend.dto.PaymentIntentRequest;
import org.example.elearning_backend.dto.PaymentIntentResponse;
import org.example.elearning_backend.security.UserDetailsImpl;
import org.example.elearning_backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Create a payment intent for course purchase
     */
    @PostMapping("/create-payment-intent")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            PaymentIntentResponse response = paymentService.createPaymentIntent(
                    request.getCourseId(),
                    currentUser.getId(),
                    request.getCurrency()
            );

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity
                    .badRequest()
                    .body("Stripe error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Get publishable key for frontend
     */
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok()
                .body("{\"publishableKey\": \"" + System.getenv("STRIPE_PUBLISHABLE_KEY") + "\"}");
    }
}