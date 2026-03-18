package org.example.elearning_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Send verification email to the new user that has been registered
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify Your Email - E-Learning Platform";
        String verificationLink = baseUrl + "/api/auth/verify-email?token=" + token;

        String message = String.format(
                "Hello!\n\n" +
                        "Thank you for registering with our E-Learning Platform. " +
                        "Please click the link below to verify your email address:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't register for an account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "E-Learning Team",
                verificationLink
        );

        sendEmail(toEmail, subject, message);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Password Reset Request - E-Learning Platform";
        String resetLink = baseUrl + "/api/auth/reset-password?token=" + token;

        String message = String.format(
                "Hello!\n\n" +
                        "We received a request to reset your password. " +
                        "Click the link below to set a new password:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't request a password reset, please ignore this email " +
                        "and make sure you can still access your account.\n\n" +
                        "Best regards,\n" +
                        "E-Learning Team",
                resetLink
        );

        sendEmail(toEmail, subject, message);
    }

    /**
     * Generic email sender
     */
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}