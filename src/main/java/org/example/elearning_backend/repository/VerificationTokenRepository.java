package org.example.elearning_backend.repository;

import org.example.elearning_backend.model.TokenType;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserAndTokenTypeAndUsedFalse(User user, TokenType tokenType);

    void deleteByUser(User user);
}