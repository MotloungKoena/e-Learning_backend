package org.example.elearning_backend.controller;

import org.example.elearning_backend.model.Enrollment;
import org.example.elearning_backend.security.UserDetailsImpl;
import org.example.elearning_backend.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Enroll in a course (STUDENT only)
     */
    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enrollInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            Enrollment enrollment = enrollmentService.enrollStudent(currentUser.getId(), courseId);
            return ResponseEntity.ok(enrollment);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error enrolling in course: " + e.getMessage());
        }
    }

    /**
     * Get all enrollments for current student (My Courses)
     */
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Enrollment>> getMyEnrollments(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(currentUser.getId());
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get students enrolled in a course (INSTRUCTOR only - must own course)
     */
    @GetMapping("/course/{courseId}/students")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getCourseStudents(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(courseId, currentUser.getId());
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error fetching students: " + e.getMessage());
        }
    }

    /**
     * Update progress in a course (STUDENT only)
     */
    @PutMapping("/{enrollmentId}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long enrollmentId,
            @RequestParam Integer progress,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            Enrollment enrollment = enrollmentService.updateProgress(enrollmentId, progress, currentUser.getId());
            return ResponseEntity.ok(enrollment);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error updating progress: " + e.getMessage());
        }
    }

    /**
     * Check if enrolled in a specific course
     */
    @GetMapping("/check/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> checkEnrollment(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        boolean isEnrolled = enrollmentService.isEnrolled(currentUser.getId(), courseId);
        return ResponseEntity.ok().body("{\"enrolled\": " + isEnrolled + "}");
    }

    /**
     * Get enrollment count for a course (public info)
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<?> getEnrollmentCount(@PathVariable Long courseId) {
        try {
            Long count = enrollmentService.getEnrollmentCount(courseId);
            return ResponseEntity.ok().body("{\"count\": " + count + "}");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Unenroll from a course (STUDENT only)
     */
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> unenroll(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            enrollmentService.unenroll(enrollmentId, currentUser.getId());
            return ResponseEntity.ok("Successfully unenrolled from course");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error unenrolling: " + e.getMessage());
        }
    }
}