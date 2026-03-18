package org.example.elearning_backend.service;

import org.example.elearning_backend.model.Role;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.model.UserStatus;
import org.example.elearning_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
// Add these imports at the top
import org.example.elearning_backend.model.TokenType;
import org.example.elearning_backend.model.VerificationToken;
import org.example.elearning_backend.repository.VerificationTokenRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register a new user
    /*public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }*/

    // Find user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Update user status
    public User updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        return userRepository.save(user);
    }

    // Get all instructors
    public List<User> getAllInstructors() {
        return userRepository.findByRole(Role.INSTRUCTOR);
    }

    // Get all students
    public List<User> getAllStudents() {
        return userRepository.findByRole(Role.STUDENT);
    }

    // Get all admins
    public List<User> getAllAdmins() {
        return userRepository.findByRole(Role.ADMIN);
    }

    // Find users by status
    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    // Add this method to UserService.java
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }



    // Add these autowired dependencies
    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Register a new user with email verification
     */
    @Transactional
    public User registerUserWithVerification(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use!");
        }

        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default status to PENDING (waiting for email verification)
        user.setStatus(UserStatus.PENDING);

        // Save the user
        User savedUser = userRepository.save(user);

        // Create verification token
        VerificationToken token = new VerificationToken(savedUser, TokenType.EMAIL_VERIFICATION);
        tokenRepository.save(token);

        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), token.getToken());

        return savedUser;
    }

    /**
     * Verify email with token
     */
    @Transactional
    public User verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        // Check if token is expired
        if (verificationToken.isExpired()) {
            throw new RuntimeException("Verification token has expired");
        }

        // Check if already used
        if (verificationToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        User user = verificationToken.getUser();

        // Update user status to ACTIVE
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Mark token as used
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        // Delete any other unused tokens for this user
        tokenRepository.deleteByUser(user);

        return user;
    }

    /**
     * Create password reset token
     */
    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Delete any existing reset tokens for this user
        tokenRepository.findByUserAndTokenTypeAndUsedFalse(user, TokenType.PASSWORD_RESET)
                .ifPresent(tokenRepository::delete);

        // Create new reset token
        VerificationToken resetToken = new VerificationToken(user, TokenType.PASSWORD_RESET);
        tokenRepository.save(resetToken);

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
    }

    /**
     * Reset password with token
     */
    @Transactional
    public User resetPassword(String token, String newPassword) {
        VerificationToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        // Check if token is expired
        if (resetToken.isExpired()) {
            throw new RuntimeException("Reset token has expired");
        }

        // Check if already used
        if (resetToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        // Verify token type
        if (resetToken.getTokenType() != TokenType.PASSWORD_RESET) {
            throw new RuntimeException("Invalid token type");
        }

        User user = resetToken.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return user;
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if already verified
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Email already verified");
        }

        // Delete old verification tokens
        tokenRepository.findByUserAndTokenTypeAndUsedFalse(user, TokenType.EMAIL_VERIFICATION)
                .ifPresent(tokenRepository::delete);

        // Create new token
        VerificationToken newToken = new VerificationToken(user, TokenType.EMAIL_VERIFICATION);
        tokenRepository.save(newToken);

        // Send new verification email
        emailService.sendVerificationEmail(user.getEmail(), newToken.getToken());
    }
}