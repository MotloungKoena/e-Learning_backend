package org.example.elearning_backend.model;

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
}

