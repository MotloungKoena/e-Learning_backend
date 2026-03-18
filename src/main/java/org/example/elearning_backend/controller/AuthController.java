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
// Add these imports
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow requests from React frontend
public class AuthController {

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

    /**
     * Login endpoint - Authenticates user and returns JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
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

            // Get the full user from database to get name
            User user = userRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

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
                    .body("Error: Invalid email or password");
        }
    }

    /**
     * Register endpoint - Creates a new user account
     */
    /*@PostMapping("/register")
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
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setCreatedAt(LocalDateTime.now());

            // Set role (default to STUDENT if not specified)
            String roleStr = registerRequest.getRole();
            if (roleStr != null && roleStr.equalsIgnoreCase("INSTRUCTOR")) {
                user.setRole(Role.INSTRUCTOR);
                // Instructors need admin approval
                user.setStatus(UserStatus.PENDING);
            } else {
                user.setRole(Role.STUDENT);
                user.setStatus(UserStatus.ACTIVE);
            }

            // Save user to database
            userRepository.save(user);

            return ResponseEntity.ok("User registered successfully!");

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }*/

    /**
     * Test endpoint - Check if authentication is working
     */
    @GetMapping("/test")
    public String test() {
        return "Auth endpoint is working!";
    }

    /**
     * Register endpoint with email verification
     */
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
            userService.registerUserWithVerification(user);

            return ResponseEntity.ok("User registered successfully! Please check your email to verify your account.");

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Verify email endpoint
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            User user = userService.verifyEmail(token);
            return ResponseEntity.ok("Email verified successfully! You can now log in.");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error verifying email: " + e.getMessage());
        }
    }

    /**
     * Request password reset
     */
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

    /**
     * Reset password
     */
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

    /**
     * Resend verification email
     */
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