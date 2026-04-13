package org.example.elearning_backend.controller;

import org.example.elearning_backend.model.User;
import org.example.elearning_backend.model.UserStatus;
import org.example.elearning_backend.security.UserDetailsImpl;
import org.example.elearning_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }


    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveInstructor(@PathVariable Long userId) {
        userService.updateUserStatus(userId, UserStatus.ACTIVE);
        return ResponseEntity.ok("Instructor approved");
    }

    @PutMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockUser(@PathVariable Long userId) {
        userService.updateUserStatus(userId, UserStatus.BLOCKED);
        return ResponseEntity.ok("User blocked");
    }

    @PutMapping("/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unblockUser(@PathVariable Long userId) {
        userService.updateUserStatus(userId, UserStatus.ACTIVE);
        return ResponseEntity.ok("User unblocked");
    }


    @GetMapping("/instructors")
    public List<User> getInstructors() {
        return userService.getAllInstructors();
    }

    @GetMapping("/students")
    public List<User> getStudents() {
        return userService.getAllStudents();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        try {
            User user = userService.getUserById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("role", user.getRole().name());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }
}