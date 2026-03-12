package org.example.elearning_backend.repository;
import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.Enrollment;
import org.example.elearning_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Find all enrollments for a specific student
    List<Enrollment> findByStudent(User student);

    // Find all enrollments for a specific course
    List<Enrollment> findByCourse(Course course);

    // Check if a student is enrolled in a specific course
    Optional<Enrollment> findByStudentAndCourse(User student, Course course);

    // Check if enrollment exists
    boolean existsByStudentAndCourse(User student, Course course);

    // Count how many students are enrolled in a course
    Long countByCourse(Course course);

    // Add this method to check if a student is enrolled in a course
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}