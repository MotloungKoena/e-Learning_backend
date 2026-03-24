package org.example.elearning_backend.service;

import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.Enrollment;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.repository.CourseRepository;
import org.example.elearning_backend.repository.EnrollmentRepository;
import org.example.elearning_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Enroll a student in a course
     */
    public Enrollment enrollStudent(Long studentId, Long courseId) {
        // Check if already enrolled
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Already enrolled in this course");
        }

        // Get student and course
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if course is published
        if (!course.getStatus().toString().equals("PUBLISHED")) {
            throw new RuntimeException("Cannot enroll in unpublished course");
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgress(0);
        enrollment.setCompleted(false);

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Get all enrollments for a student
     */
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return enrollmentRepository.findByStudent(student);
    }

    /**
     * Get all students enrolled in a course (for instructors)
     */
    public List<Enrollment> getCourseEnrollments(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Verify instructor owns this course
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("You don't have permission to view this course's enrollments");
        }

        return enrollmentRepository.findByCourse(course);
    }

    /**
     * Update student's progress in a course
     */
    public Enrollment updateProgress(Long enrollmentId, Integer progress, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Verify this enrollment belongs to the student
        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("You don't have permission to update this enrollment");
        }

        // Validate progress (0-100)
        if (progress < 0 || progress > 100) {
            throw new RuntimeException("Progress must be between 0 and 100");
        }

        enrollment.setProgress(progress);

        // If progress is 100, mark as completed
        if (progress == 100) {
            enrollment.setCompleted(true);
        }

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Check if a student is enrolled in a course
     */
    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    /**
     * Get enrollment count for a course
     */
    public Long getEnrollmentCount(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return enrollmentRepository.countByCourse(course);
    }

    /**
     * Unenroll from a course (students can drop courses)
     */
    public void unenroll(Long enrollmentId, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Verify this enrollment belongs to the student
        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("You don't have permission to unenroll");
        }

        enrollmentRepository.delete(enrollment);
    }


}