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

    // Add @JsonIgnore here to break the infinite loop
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

/*package org.example.elearning_backend.model;

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

    // @Lob means this can hold large text (like descriptions)
    @Lob
    private String description;

    private String category;

    private Double price;

    // Course status - DRAFT (instructor still working) or PUBLISHED (available to students)
    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Many courses can belong to one instructor
    // @ManyToOne means many courses can have the same instructor
    @ManyToOne
    @JoinColumn(name = "instructor_id")  // foreign key column name
    private User instructor;

    // One course can have many enrollments (students)
    // mappedBy tells JPA that 'course' field in Enrollment class owns the relationship
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;

    // One course can have many materials (videos, PDFs)
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Material> materials;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = CourseStatus.DRAFT;  // New courses start as DRAFT
        }
    }
}*/