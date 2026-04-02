package org.example.elearning_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // Existing method - keep as is
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

    // Existing method - keep as is
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

    // ============ NEW METHODS ============

    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "Welcome to E-Learning Platform!";
        String htmlContent = buildWelcomeEmailHtml(name);
        sendHtmlEmail(toEmail, subject, htmlContent);
        logger.info("Welcome email sent to: {}", toEmail);
    }

    public void sendEnrollmentEmail(String toEmail, String studentName, String courseTitle,
                                    String instructorName, Long courseId) {
        String subject = "Course Enrollment Confirmation";
        String htmlContent = buildEnrollmentEmailHtml(studentName, courseTitle, instructorName, courseId);
        sendHtmlEmail(toEmail, subject, htmlContent);
        logger.info("Enrollment email sent to: {}", toEmail);
    }

    public void sendCompletionEmail(String toEmail, String studentName, String courseTitle,
                                    Long enrollmentId) {
        String subject = "Course Completion Certificate";
        String certificateLink = frontendUrl + "/my-learning";
        String htmlContent = buildCompletionEmailHtml(studentName, courseTitle, certificateLink);
        sendHtmlEmail(toEmail, subject, htmlContent);
        logger.info("Completion email sent to: {}", toEmail);
    }

    // HTML builders
    private String buildWelcomeEmailHtml(String name) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome to E-Learning Platform</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="header"><h1>Welcome to E-Learning Platform!</h1></div>
                <div class="content">
                    <h2>Hello %s!</h2>
                    <p>Thank you for joining our learning community. We're excited to have you on board!</p>
                    <p>Here's what you can do:</p>
                    <ul>
                        <li>📚 Browse hundreds of courses</li>
                        <li>🎯 Track your learning progress</li>
                        <li>🏆 Earn certificates upon completion</li>
                        <li>💬 Rate and review courses</li>
                    </ul>
                    <div style="text-align: center;">
                        <a href="http://localhost:3000/courses" class="button">Start Learning Now</a>
                    </div>
                </div>
                <div class="footer"><p>&copy; 2025 E-Learning Platform. All rights reserved.</p></div>
            </body>
            </html>
            """, name);
    }

    private String buildEnrollmentEmailHtml(String studentName, String courseTitle, String instructorName, Long courseId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Course Enrollment Confirmation</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #10B981 0%, #059669 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #10B981; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="header"><h1>🎉 Enrollment Confirmed!</h1></div>
                <div class="content">
                    <h2>Hello %s!</h2>
                    <p>You have successfully enrolled in:</p>
                    <div style="background: #f0fdf4; padding: 15px; border-radius: 8px; margin: 15px 0;">
                        <h3 style="margin: 0 0 5px 0; color: #059669;">📖 %s</h3>
                        <p style="margin: 0; color: #666;">Instructor: %s</p>
                    </div>
                    <p>Start learning today and track your progress!</p>
                    <div style="text-align: center;">
                        <a href="http://localhost:3000/courses/%d" class="button">Go to Course</a>
                    </div>
                </div>
                <div class="footer"><p>&copy; 2025 E-Learning Platform. All rights reserved.</p></div>
            </body>
            </html>
            """, studentName, courseTitle, instructorName, courseId);
    }

    private String buildCompletionEmailHtml(String studentName, String courseTitle, String certificateLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Course Completion Certificate</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #F59E0B; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="header"><h1>🏆 Congratulations!</h1></div>
                <div class="content">
                    <div style="text-align: center; font-size: 48px; margin: 20px 0;">🎓</div>
                    <h2>Hello %s!</h2>
                    <p>Congratulations on completing:</p>
                    <div style="background: #fffbeb; padding: 15px; border-radius: 8px; margin: 15px 0;">
                        <h3 style="margin: 0; color: #D97706;">📖 %s</h3>
                    </div>
                    <p>You've successfully completed all course materials and earned your certificate!</p>
                    <div style="text-align: center;">
                        <a href="%s" class="button">📜 Download Your Certificate</a>
                    </div>
                    <p style="margin-top: 20px;">Share your achievement on social media and keep learning!</p>
                </div>
                <div class="footer"><p>&copy; 2025 E-Learning Platform. All rights reserved.</p></div>
            </body>
            </html>
            """, studentName, courseTitle, certificateLink);
    }

    // Plain text email (keep existing)
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

    // HTML email sender
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("HTML Email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}