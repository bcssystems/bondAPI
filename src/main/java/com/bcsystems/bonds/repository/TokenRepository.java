package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    @Query("SELECT t FROM Token t WHERE t.persona.idPersona = :personaId AND t.isExpired = false AND t.isRevoked = false")
    List<Token> findAllValidTokensByPersona(Integer personaId);

    Optional<Token> findByToken(String token);
    Optional<Token> findByRefreshToken(String refreshToken);
}
