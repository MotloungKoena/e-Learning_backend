package org.example.elearning_backend.repository;
import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.FileType;
import org.example.elearning_backend.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Find all materials for a specific course
    List<Material> findByCourse(Course course);

    // Find materials by file type (VIDEO, PDF, etc.)
    List<Material> findByFileType(FileType fileType);

    // Find materials by course ID
    List<Material> findByCourseId(Long courseId);

    // Add this method to order materials
    List<Material> findByCourseOrderByOrderIndexAsc(Course course);
}