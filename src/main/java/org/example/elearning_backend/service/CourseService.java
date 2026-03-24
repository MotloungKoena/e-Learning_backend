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

    public Course createCourse(Course course, Long instructorId) {
        // Find the instructor
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Set course details
        course.setInstructor(instructor);
        course.setCreatedAt(LocalDateTime.now());
        course.setStatus(CourseStatus.DRAFT); // New courses start as DRAFT

        return courseRepository.save(course);
    }

    public List<Course> getAllPublishedCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByInstructor(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        return courseRepository.findByInstructor(instructor);
    }

    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
    }

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

    public Course publishCourse(Long courseId, Long instructorId) {
        Course course = getCourseById(courseId);

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("You don't have permission to publish this course");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        return courseRepository.save(course);
    }

    public void deleteCourse(Long courseId, Long userId, String userRole) {
        Course course = getCourseById(courseId);

        // Admin can delete any course, Instructors can only delete their own
        if (!userRole.equals("ADMIN") && !course.getInstructor().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this course");
        }

        courseRepository.delete(course);
    }

    public List<Course> searchCourses(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<Course> getCoursesByCategory(String category) {
        return courseRepository.findByCategory(category);
    }
}