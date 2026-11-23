package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {

    Optional<Session> findByTokenHash(String tokenHash);

    Optional<Session> findByTokenHashAndFechaExpiracionAfter(String tokenHash, LocalDateTime now);

    void deleteByTokenHash(String tokenHash);

    void deleteByUserId(Long userId);
}
