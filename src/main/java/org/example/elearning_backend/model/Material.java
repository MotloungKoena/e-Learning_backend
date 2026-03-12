package org.example.elearning_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;  // VIDEO, PDF, DOCUMENT, etc.

    @Column(name = "file_url")
    private String fileUrl;  // Path where file is stored

    @Column(name = "file_size")
    private Long fileSize;  // Size in bytes

    private Integer duration;  // For videos: length in minutes

    @Column(name = "order_index")
    private Integer orderIndex;  // To order materials within a course

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Many materials belong to one course
    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnore
    private Course course;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (orderIndex == null) {
            orderIndex = 0;
        }
    }
}


/*package org.example.elearning_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;  // VIDEO, PDF, DOCUMENT

    @Column(name = "file_url")
    private String fileUrl;  // Path where file is stored

    private Integer duration;  // For videos: length in minutes

    // Many materials belong to one course
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}*/

