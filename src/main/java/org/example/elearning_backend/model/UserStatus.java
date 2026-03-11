package org.example.elearning_backend.model;

public enum UserStatus {
    PENDING,  // Instructor waiting for admin approval
    ACTIVE,   // Normal active user
    BLOCKED   // User who has been blocked
}
