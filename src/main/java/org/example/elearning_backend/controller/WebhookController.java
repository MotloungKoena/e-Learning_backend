package org.example.elearning_backend.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.example.elearning_backend.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Verify webhook signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.error("Invalid signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;

            case "payment_intent.created":
                logger.info("Payment intent created");
                break;

            default:
                logger.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok().build();
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (paymentIntent != null) {
            logger.info("Payment succeeded for intent: {}", paymentIntent.getId());

            try {
                // Enroll the student
                paymentService.handleSuccessfulPayment(paymentIntent.getId());
                logger.info("Student enrolled successfully!");
            } catch (Exception e) {
                logger.error("Failed to enroll student: {}", e.getMessage());
            }
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (paymentIntent != null) {
            logger.error("Payment failed for intent: {}", paymentIntent.getId());
            // TODO: Handle failed payment (notify the user maybe here)
        }
    }
}