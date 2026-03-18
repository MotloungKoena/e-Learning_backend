package org.example.elearning_backend.service;

import org.example.elearning_backend.dto.CourseRatingSummary;
import org.example.elearning_backend.dto.RatingRequest;
import org.example.elearning_backend.dto.RatingResponse;
import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.Rating;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.repository.CourseRepository;
import org.example.elearning_backend.repository.RatingRepository;
import org.example.elearning_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Add or update a rating for a course
     */
    @Transactional
    public RatingResponse rateCourse(Long courseId, Long studentId, RatingRequest request) {

        // Check if student is enrolled in the course
        if (!enrollmentService.isEnrolled(studentId, courseId)) {
            throw new RuntimeException("You must be enrolled in this course to rate it");
        }

        // Get student and course
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if student already rated this course
        Rating rating = ratingRepository.findByStudentAndCourse(student, course)
                .orElse(new Rating());

        // Set or update rating details
        rating.setStudent(student);
        rating.setCourse(course);
        rating.setRating(request.getRating());
        rating.setReview(request.getReview());

        // Save rating
        Rating savedRating = ratingRepository.save(rating);

        // Convert to response DTO
        return convertToResponse(savedRating);
    }

    /**
     * Get all ratings for a course
     */
    public List<RatingResponse> getCourseRatings(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return ratingRepository.findByCourse(course).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get rating summary for a course
     */
    public CourseRatingSummary getCourseRatingSummary(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<Rating> ratings = ratingRepository.findByCourse(course);

        Double averageRating = ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);

        Long totalRatings = (long) ratings.size();

        // Count ratings by star value
        Long fiveStar = ratings.stream().filter(r -> r.getRating() == 5).count();
        Long fourStar = ratings.stream().filter(r -> r.getRating() == 4).count();
        Long threeStar = ratings.stream().filter(r -> r.getRating() == 3).count();
        Long twoStar = ratings.stream().filter(r -> r.getRating() == 2).count();
        Long oneStar = ratings.stream().filter(r -> r.getRating() == 1).count();

        return new CourseRatingSummary(
                Math.round(averageRating * 10) / 10.0,  // Round to 1 decimal
                totalRatings,
                fiveStar, fourStar, threeStar, twoStar, oneStar
        );
    }

    /**
     * Get rating by ID
     */
    public RatingResponse getRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        return convertToResponse(rating);
    }

    /**
     * Delete a rating (student can delete their own, admin can delete any)
     */
    @Transactional
    public void deleteRating(Long ratingId, Long userId, String userRole) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));

        // Check permissions: Student can delete own rating, Admin can delete any
        if (!userRole.equals("ADMIN") && !rating.getStudent().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this rating");
        }

        ratingRepository.delete(rating);
    }

    /**
     * Check if a student has rated a course
     */
    public boolean hasRated(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return ratingRepository.existsByStudentAndCourse(student, course);
    }

    /**
     * Convert Rating entity to RatingResponse DTO
     */
    private RatingResponse convertToResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getStudent().getId(),
                rating.getStudent().getFirstName() + " " + rating.getStudent().getLastName(),
                rating.getRating(),
                rating.getReview(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}