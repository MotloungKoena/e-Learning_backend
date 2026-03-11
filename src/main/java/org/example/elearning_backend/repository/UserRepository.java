package org.example.elearning_backend.repository;

import org.example.elearning_backend.model.Role;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if email exists
    Boolean existsByEmail(String email);

    // Find users by role - THIS IS THE CORRECT METHOD NAME
    List<User> findByRole(Role role);

    // Find users by status
    List<User> findByStatus(UserStatus status);
}