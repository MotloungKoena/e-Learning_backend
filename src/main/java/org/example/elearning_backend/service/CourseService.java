package org.example.elearning_backend.service;

import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.CourseStatus;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.repository.CourseRepository;
import org.example.elearning_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new course (Instructors only)
     */
    public Course createCourse(Course course, Long instructorId) {
        // Find the instructor
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Set course details
        course.setInstructor(instructor);
        course.setCreatedAt(LocalDateTime.now());
        course.setStatus(CourseStatus.DRAFT); // New courses start as DRAFT

        // Save and return
        return courseRepository.save(course);
    }

    /**
     * Get all published courses (for students to browse)
     */
    public List<Course> getAllPublishedCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED);
    }

    /**
     * Get all courses (for admin)
     */
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Get courses by instructor
     */
    public List<Course> getCoursesByInstructor(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        return courseRepository.findByInstructor(instructor);
    }

    /**
     * Get course by ID
     */
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
    }

    /**
     * Update course (Instructor only)
     */
    public Course updateCourse(Long courseId, Course courseDetails, Long instructorId) {
        Course course = getCourseById(courseId);

        // Verify this instructor owns the course
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("You don't have permission to edit this course");
        }

        // Update fields
        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setCategory(courseDetails.getCategory());
        course.setPrice(courseDetails.getPrice());

        return courseRepository.save(course);
    }

    /**
     * Publish course (Instructor only)
     */
    public Course publishCourse(Long courseId, Long instructorId) {
        Course course = getCourseById(courseId);

        // Verify ownership
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("You don't have permission to publish this course");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        return courseRepository.save(course);
    }

    /**
     * Delete course (Instructor or Admin)
     */
    public void deleteCourse(Long courseId, Long userId, String userRole) {
        Course course = getCourseById(courseId);

        // Admin can delete any course, Instructors can only delete their own
        if (!userRole.equals("ADMIN") && !course.getInstructor().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this course");
        }

        courseRepository.delete(course);
    }

    /**
     * Search courses by title
     */
    public List<Course> searchCourses(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword);
    }

    /**
     * Get courses by category
     */
    public List<Course> getCoursesByCategory(String category) {
        return courseRepository.findByCategory(category);
    }
}