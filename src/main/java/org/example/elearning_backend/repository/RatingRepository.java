package org.example.elearning_backend.repository;

import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.Rating;
import org.example.elearning_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Find all ratings for a course
    List<Rating> findByCourse(Course course);

    // Find rating by student and course (to check if already rated)
    Optional<Rating> findByStudentAndCourse(User student, Course course);

    // Check if student already rated a course
    boolean existsByStudentAndCourse(User student, Course course);

    // Get average rating for a course
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.course = :course")
    Double getAverageRatingForCourse(@Param("course") Course course);

    // Get rating count for a course
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.course = :course")
    Long getRatingCountForCourse(@Param("course") Course course);

    // Find all ratings by a student
    List<Rating> findByStudent(User student);
}