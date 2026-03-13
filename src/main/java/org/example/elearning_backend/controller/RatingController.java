package org.example.elearning_backend.controller;

import org.example.elearning_backend.dto.CourseRatingSummary;
import org.example.elearning_backend.dto.RatingRequest;
import org.example.elearning_backend.dto.RatingResponse;
import org.example.elearning_backend.security.UserDetailsImpl;
import org.example.elearning_backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    /**
     * Rate a course (STUDENT only - must be enrolled)
     */
    @PostMapping("/courses/{courseId}/ratings")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> rateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            RatingResponse response = ratingService.rateCourse(courseId, currentUser.getId(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error rating course: " + e.getMessage());
        }
    }

    /**
     * Update an existing rating (STUDENT only)
     */
    @PutMapping("/ratings/{ratingId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateRating(
            @PathVariable Long ratingId,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            // Note: We're reusing the rateCourse method since it handles both create and update
            // But we need the courseId - we could add a separate update method if needed
            RatingResponse response = ratingService.getRating(ratingId);
            if (!response.getStudentId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest()
                        .body("You can only update your own ratings");
            }

            // Extract courseId from the rating and call rateCourse again
            // For simplicity, we'll just return a message to use POST with same course
            return ResponseEntity.badRequest()
                    .body("To update, please POST to /courses/{courseId}/ratings with the same course ID");

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Get all ratings for a course (PUBLIC)
     */
    @GetMapping("/courses/{courseId}/ratings")
    public ResponseEntity<?> getCourseRatings(@PathVariable Long courseId) {
        try {
            List<RatingResponse> ratings = ratingService.getCourseRatings(courseId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching ratings: " + e.getMessage());
        }
    }

    /**
     * Get rating summary for a course (PUBLIC)
     */
    @GetMapping("/courses/{courseId}/ratings/summary")
    public ResponseEntity<?> getCourseRatingSummary(@PathVariable Long courseId) {
        try {
            CourseRatingSummary summary = ratingService.getCourseRatingSummary(courseId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching rating summary: " + e.getMessage());
        }
    }

    /**
     * Get a specific rating by ID (PUBLIC)
     */
    @GetMapping("/ratings/{ratingId}")
    public ResponseEntity<?> getRating(@PathVariable Long ratingId) {
        try {
            RatingResponse rating = ratingService.getRating(ratingId);
            return ResponseEntity.ok(rating);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching rating: " + e.getMessage());
        }
    }

    /**
     * Check if current student has rated a course
     */
    @GetMapping("/courses/{courseId}/ratings/my-rating")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> hasRated(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            boolean hasRated = ratingService.hasRated(currentUser.getId(), courseId);
            return ResponseEntity.ok().body("{\"hasRated\": " + hasRated + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete a rating (STUDENT - own ratings, ADMIN - any)
     */
    @DeleteMapping("/ratings/{ratingId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteRating(
            @PathVariable Long ratingId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            ratingService.deleteRating(ratingId, currentUser.getId(), currentUser.getAuthorities().toString());
            return ResponseEntity.ok("Rating deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error deleting rating: " + e.getMessage());
        }
    }
}