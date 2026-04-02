package org.example.elearning_backend.controller;

import org.example.elearning_backend.dto.JwtResponse;
import org.example.elearning_backend.dto.LoginRequest;
import org.example.elearning_backend.dto.RegisterRequest;
import org.example.elearning_backend.model.Role;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.model.UserStatus;
import org.example.elearning_backend.repository.UserRepository;
import org.example.elearning_backend.security.JwtUtils;
import org.example.elearning_backend.security.UserDetailsImpl;
import org.example.elearning_backend.service.EmailService;
import org.example.elearning_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/test")
    public String test() {
        return "Auth endpoint is working!";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // First, check if user exists and is verified
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: User not found");
            }

            // Check if email is verified
            if (user.getStatus() == UserStatus.PENDING) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: Please verify your email before logging in. Check your inbox for the verification link.");
            }

            // Check if user is blocked
            if (user.getStatus() == UserStatus.BLOCKED) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: Your account has been blocked. Please contact administrator.");
            }

            // Create authentication token with email and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Get user details
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Return response with token and user info
            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getEmail(),
                    user.getRole().name(),
                    user.getFirstName()
            ));

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Invalid email or password.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: Email is already in use!");
            }

            // Create new user
            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword()); // Will be encoded in service
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());

            // Set role
            String roleStr = registerRequest.getRole();
            if (roleStr != null && roleStr.equalsIgnoreCase("INSTRUCTOR")) {
                user.setRole(Role.INSTRUCTOR);
            } else {
                user.setRole(Role.STUDENT);
            }

            // Register with verification
            User savedUser = userService.registerUserWithVerification(user);

            // Send welcome email after successful registration
            try {
                emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());
                logger.info("Welcome email sent to: {}", savedUser.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send welcome email to {}: {}", savedUser.getEmail(), e.getMessage());
            }

            return ResponseEntity.ok("User registered successfully! Please check your email to verify your account.");

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            User user = userService.verifyEmail(token);
            String successMessage = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Email Verified</title>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }\n" +
                    "        .success { color: green; font-size: 24px; margin: 20px; }\n" +
                    "        .message { color: #333; font-size: 18px; margin: 20px; }\n" +
                    "        .login-link { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class='success'>✅ Email Verified Successfully!</div>\n" +
                    "    <div class='message'>Your email has been verified. You can now log in to your account.</div>\n" +
                    "    <a href='http://localhost:3000/login' class='login-link'>Go to Login</a>\n" +
                    "</body>\n" +
                    "</html>";

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html")
                    .body(successMessage);

        } catch (Exception e) {
            String errorMessage = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Verification Failed</title>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }\n" +
                    "        .error { color: red; font-size: 24px; margin: 20px; }\n" +
                    "        .message { color: #333; font-size: 18px; margin: 20px; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class='error'>❌ Verification Failed</div>\n" +
                    "    <div class='message'>" + e.getMessage() + "</div>\n" +
                    "</body>\n" +
                    "</html>";

            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html")
                    .body(errorMessage);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            userService.createPasswordResetToken(email);
            return ResponseEntity.ok("Password reset email sent. Please check your inbox.");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            User user = userService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password reset successfully! You can now log in with your new password.");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error resetting password: " + e.getMessage());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            userService.resendVerificationEmail(email);
            return ResponseEntity.ok("Verification email resent. Please check your inbox.");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
}