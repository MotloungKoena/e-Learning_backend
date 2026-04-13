package org.example.elearning_backend.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.example.elearning_backend.dto.PaymentIntentResponse;
import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.repository.CourseRepository;
import org.example.elearning_backend.repository.EnrollmentRepository;
import org.example.elearning_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    public PaymentIntentResponse createPaymentIntent(Long courseId, Long studentId, String currency)
            throws StripeException {

        // Get the course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if course is published
        if (!course.getStatus().toString().equals("PUBLISHED")) {
            throw new RuntimeException("Course is not available for purchase");
        }

        // Check if student is already enrolled
        if (enrollmentService.isEnrolled(studentId, courseId)) {
            throw new RuntimeException("Already enrolled in this course");
        }

        Long amount = (long) (course.getPrice() * 100);

        // Create payment intent parameters
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency != null ? currency : "usd")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("courseId", String.valueOf(courseId))
                .putMetadata("studentId", String.valueOf(studentId))
                .putMetadata("courseTitle", course.getTitle())
                .build();

        // Create the payment intent
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Return response with client secret
        return new PaymentIntentResponse(
                paymentIntent.getClientSecret(),
                paymentIntent.getId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency(),
                paymentIntent.getStatus()
        );
    }

    @Transactional
    public void handleSuccessfulPayment(String paymentIntentId) {
        try {
            // Retrieve the payment intent
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Get metadata
            String courseIdStr = paymentIntent.getMetadata().get("courseId");
            String studentIdStr = paymentIntent.getMetadata().get("studentId");

            if (courseIdStr == null || studentIdStr == null) {
                throw new RuntimeException("Missing metadata in payment intent");
            }

            Long courseId = Long.parseLong(courseIdStr);
            Long studentId = Long.parseLong(studentIdStr);

            // Check if already enrolled (prevent duplicate)
            if (enrollmentService.isEnrolled(studentId, courseId)) {
                return; // Already enrolled, nothing to do
            }

            // Enroll the student
            enrollmentService.enrollStudent(studentId, courseId);

            // TODO: Send confirmation email
            // TODO: Update payment record in databases

        } catch (Exception e) {
            throw new RuntimeException("Failed to process successful payment: " + e.getMessage());
        }
    }
}