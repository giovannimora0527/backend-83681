package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.MedicoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MedicoUsuarioRepository extends JpaRepository<MedicoUsuario, Long> {
    Optional<MedicoUsuario> findByUsuarioId(Long usuarioId);
}
