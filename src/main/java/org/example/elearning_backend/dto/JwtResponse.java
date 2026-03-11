package org.example.elearning_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;           // JWT token
    private String type = "Bearer"; // Token type
    private Long id;                // User ID
    private String email;           // User email
    private String role;            // User role
    private String firstName;       // User's first name

    // Constructor without type (type is always "Bearer")
    public JwtResponse(String token, Long id, String email, String role, String firstName) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.type = "Bearer";
    }
}