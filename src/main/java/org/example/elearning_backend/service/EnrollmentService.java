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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

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
        enrollment.setUpdatedAt(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // Send enrollment confirmation email
        try {
            emailService.sendEnrollmentEmail(
                    student.getEmail(),
                    student.getFirstName(),
                    course.getTitle(),
                    course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName(),
                    courseId
            );
            logger.info("Enrollment email sent to: {}", student.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send enrollment email to {}: {}", student.getEmail(), e.getMessage());
        }

        return savedEnrollment;
    }

    public List<Enrollment> getStudentEnrollments(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return enrollmentRepository.findByStudent(student);
    }

    public List<Enrollment> getCourseEnrollments(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Verify instructor owns this course
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("You don't have permission to view this course's enrollments");
        }

        return enrollmentRepository.findByCourse(course);
    }

    public Enrollment updateProgress(Long enrollmentId, Integer progress, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("You don't have permission to update this enrollment");
        }

        if (progress < 0 || progress > 100) {
            throw new RuntimeException("Progress must be between 0 and 100");
        }

        boolean wasCompleted = enrollment.getCompleted();

        enrollment.setProgress(progress);
        enrollment.setUpdatedAt(LocalDateTime.now());

        if (progress == 100 && !wasCompleted) {
            enrollment.setCompleted(true);

            // Send course completion email
            try {
                emailService.sendCompletionEmail(
                        enrollment.getStudent().getEmail(),
                        enrollment.getStudent().getFirstName(),
                        enrollment.getCourse().getTitle(),
                        enrollmentId
                );
                logger.info("Completion email sent to: {}", enrollment.getStudent().getEmail());
            } catch (Exception e) {
                logger.error("Failed to send completion email to {}: {}", enrollment.getStudent().getEmail(), e.getMessage());
            }
        }

        return enrollmentRepository.save(enrollment);
    }

    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    public Long getEnrollmentCount(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return enrollmentRepository.countByCourse(course);
    }

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