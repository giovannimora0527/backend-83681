package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/** Consulta y guarda los datos relacionados con un código temporal de verificación. */
@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /** Busca el registro asociado a la cuenta indicada. */
    Optional<OtpToken> findByUsuarioId(Long usuarioId);
}
