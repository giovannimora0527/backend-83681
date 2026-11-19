package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/** Consulta y guarda los datos relacionados con una sesión autenticada. */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    /** Busca el registro por el token de sesión. */
    Optional<Session> findByToken(String token);
}
