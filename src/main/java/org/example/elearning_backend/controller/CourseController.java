package org.example.elearning_backend.controller;

import org.example.elearning_backend.dto.CourseRequest;
import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.security.UserDetailsImpl;
import org.example.elearning_backend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")  // FIXED
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * Create a new course (INSTRUCTOR only)
     */
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CourseRequest courseRequest,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            // Convert CourseRequest to Course entity
            Course course = new Course();
            course.setTitle(courseRequest.getTitle());
            course.setDescription(courseRequest.getDescription());
            course.setCategory(courseRequest.getCategory());
            course.setPrice(courseRequest.getPrice());

            Course savedCourse = courseService.createCourse(course, currentUser.getId());
            return ResponseEntity.ok(savedCourse);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error creating course: " + e.getMessage());
        }
    }

    /**
     * Get all published courses (anyone can view)
     */
    /*@GetMapping("/published")
    public ResponseEntity<List<Course>> getPublishedCourses() {
        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }*/
    @GetMapping("/published")
    public ResponseEntity<List<Course>> getPublishedCourses() {
        System.out.println("=== GET /api/courses/published called ===");
        List<Course> courses = courseService.getAllPublishedCourses();
        System.out.println("Found " + courses.size() + " courses");
        return ResponseEntity.ok(courses);
    }

    /**
     * Get all courses (ADMIN only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    /**
     * Get courses by current instructor
     */
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<Course>> getMyCourses(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(courseService.getCoursesByInstructor(currentUser.getId()));
    }

    /**
     * Get course by ID
     */
    /*@GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable Long courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }*/
    /**
     * Get course by ID - Public endpoint (anyone can view course details)
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable Long courseId) {
        try {
            System.out.println("=== GET /api/courses/" + courseId + " called ===");
            Course course = courseService.getCourseById(courseId);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    /**
     * Update course (INSTRUCTOR only - must own the course)
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest courseRequest,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            // Convert to Course entity
            Course courseDetails = new Course();
            courseDetails.setTitle(courseRequest.getTitle());
            courseDetails.setDescription(courseRequest.getDescription());
            courseDetails.setCategory(courseRequest.getCategory());
            courseDetails.setPrice(courseRequest.getPrice());

            Course updatedCourse = courseService.updateCourse(courseId, courseDetails, currentUser.getId());
            return ResponseEntity.ok(updatedCourse);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error updating course: " + e.getMessage());
        }
    }

    /**
     * Publish course (INSTRUCTOR only)
     */
    @PutMapping("/{courseId}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> publishCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            Course publishedCourse = courseService.publishCourse(courseId, currentUser.getId());
            return ResponseEntity.ok(publishedCourse);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error publishing course: " + e.getMessage());
        }
    }

    /**
     * Delete course (INSTRUCTOR - own courses, ADMIN - any course)
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            courseService.deleteCourse(courseId, currentUser.getId(), currentUser.getAuthorities().toString());
            return ResponseEntity.ok("Course deleted successfully");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error deleting course: " + e.getMessage());
        }
    }

    /**
     * Search courses by title
     */
    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    /**
     * Get courses by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Course>> getCoursesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(courseService.getCoursesByCategory(category));
    }
}