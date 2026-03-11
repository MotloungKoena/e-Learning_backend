package org.example.elearning_backend.controller;

import org.example.elearning_backend.model.User;
import org.example.elearning_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @GetMapping("/instructors")
    public List<User> getInstructors() {
        return userService.getAllInstructors();
    }

    @GetMapping("/students")
    public List<User> getStudents() {
        return userService.getAllStudents();
    }
}