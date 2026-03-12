package org.example.elearning_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;  // Add this import
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
    @JsonIgnoreProperties({"enrollments", "password"})  // Ignore these fields when serializing
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"enrollments", "instructor"})  // Ignore these fields
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


/*package org.example.elearning_backend.model;

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

    // Many enrollments can belong to one student
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    // Many enrollments can be for one course
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    private Integer progress;  // 0 to 100 percentage

    private Boolean completed;

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
        progress = 0;
        completed = false;
    }
}*/