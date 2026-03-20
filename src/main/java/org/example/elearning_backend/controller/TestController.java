package org.example.elearning_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String testEndpoint() {
        return "yay Backend is working! You can now build your API!";
    }

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from E-Learning Platform!";
    }

    @GetMapping("/api/health")
    public String health() {
        return "Server is running! Time: " + java.time.LocalDateTime.now();
    }

    @GetMapping("/api/protected/test")
    public String protectedTest() {
        return " This is a protected endpoint - you have a valid JWT token!";
    }
}