package org.example.elearning_backend.repository;

import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.CourseStatus;
import org.example.elearning_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Find all courses by a specific instructor
    List<Course> findByInstructor(User instructor);

    // Find all published courses (available to students)
    List<Course> findByStatus(CourseStatus status);

    // Find courses by category
    List<Course> findByCategory(String category);

    // Find courses by instructor ID
    List<Course> findByInstructorId(Long instructorId);

    // Search courses by title (containing text, case insensitive)
    List<Course> findByTitleContainingIgnoreCase(String title);
}