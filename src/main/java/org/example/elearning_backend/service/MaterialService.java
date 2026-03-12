package org.example.elearning_backend.service;

import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.FileType;
import org.example.elearning_backend.model.Material;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.repository.CourseRepository;
import org.example.elearning_backend.repository.MaterialRepository;
import org.example.elearning_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    // Configure where to store uploaded files
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * Upload a file and create a material
     */
    public Material uploadMaterial(Long courseId, MultipartFile file,
                                   String title, String description,
                                   Integer duration, Integer orderIndex,
                                   Long instructorId) throws IOException {

        // Verify course exists and instructor owns it
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("You don't have permission to add materials to this course");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Determine file type
        FileType fileType = determineFileType(fileExtension);

        // Create material
        Material material = new Material();
        material.setTitle(title);
        material.setDescription(description);
        material.setFileType(fileType);
        material.setFileUrl("/uploads/" + fileName);  // URL to access the file
        material.setFileSize(file.getSize());
        material.setDuration(duration);
        material.setOrderIndex(orderIndex);
        material.setCourse(course);

        return materialRepository.save(material);
    }

    /**
     * Get all materials for a course
     */
    public List<Material> getCourseMaterials(Long courseId, Long userId, String userRole) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check access: Students can only see materials of enrolled courses
        if (userRole.equals("STUDENT")) {
            boolean isEnrolled = course.getEnrollments().stream()
                    .anyMatch(e -> e.getStudent().getId().equals(userId));
            if (!isEnrolled) {
                throw new RuntimeException("You must enroll in this course to view materials");
            }
        }

        // Instructors can see materials of their own courses
        if (userRole.equals("INSTRUCTOR") && !course.getInstructor().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to view these materials");
        }

        return materialRepository.findByCourseOrderByOrderIndexAsc(course);
    }

    /**
     * Get a single material by ID
     */
    public Material getMaterial(Long materialId, Long userId, String userRole) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        Course course = material.getCourse();

        // Check access based on role
        if (userRole.equals("STUDENT")) {
            boolean isEnrolled = course.getEnrollments().stream()
                    .anyMatch(e -> e.getStudent().getId().equals(userId));
            if (!isEnrolled) {
                throw new RuntimeException("You must be enrolled to access this material");
            }
        } else if (userRole.equals("INSTRUCTOR") && !course.getInstructor().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to access this material");
        }
        // Admin can access everything

        return material;
    }

    /**
     * Delete a material
     */
    public void deleteMaterial(Long materialId, Long instructorId) throws IOException {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        // Verify instructor owns the course
        if (!material.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("You don't have permission to delete this material");
        }

        // Delete physical file
        String fileName = material.getFileUrl().substring(material.getFileUrl().lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        Files.deleteIfExists(filePath);

        // Delete database record
        materialRepository.delete(material);
    }

    /**
     * Update material details
     */
    public Material updateMaterial(Long materialId, String title, String description,
                                   Integer duration, Integer orderIndex, Long instructorId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        // Verify instructor owns the course
        if (!material.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("You don't have permission to update this material");
        }

        if (title != null) material.setTitle(title);
        if (description != null) material.setDescription(description);
        if (duration != null) material.setDuration(duration);
        if (orderIndex != null) material.setOrderIndex(orderIndex);

        return materialRepository.save(material);
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * Determine file type based on extension
     */
    private FileType determineFileType(String extension) {
        extension = extension.toLowerCase();

        if (extension.equals(".mp4") || extension.equals(".mov") ||
                extension.equals(".avi") || extension.equals(".mkv")) {
            return FileType.VIDEO;
        } else if (extension.equals(".pdf")) {
            return FileType.PDF;
        } else if (extension.equals(".doc") || extension.equals(".docx") ||
                extension.equals(".txt") || extension.equals(".rtf")) {
            return FileType.DOCUMENT;
        } else if (extension.equals(".ppt") || extension.equals(".pptx")) {
            return FileType.PRESENTATION;
        } else if (extension.equals(".mp3") || extension.equals(".wav")) {
            return FileType.AUDIO;
        } else {
            return FileType.OTHER;
        }
    }
}