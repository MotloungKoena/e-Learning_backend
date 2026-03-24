package org.example.elearning_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"enrollments", "password"})
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"enrollments", "instructor"})
    private Course course;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    private Integer progress;

    private Boolean completed;

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
        progress = 0;
        completed = false;
    }
}