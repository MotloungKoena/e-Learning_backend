package org.example.elearning_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;  // Add this import
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private String category;

    private Double price;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    // Added @JsonIgnore here to break the infinite loop
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Material> materials;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = CourseStatus.DRAFT;
        }
    }
}
