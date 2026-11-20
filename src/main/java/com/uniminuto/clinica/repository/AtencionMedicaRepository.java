package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.AtencionMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AtencionMedicaRepository extends JpaRepository<AtencionMedica, Long> {
    Optional<AtencionMedica> findByCitaId(Long citaId);
    java.util.List<AtencionMedica> findByMascotaIdOrderByAtendidaEnDesc(Integer mascotaId);
}
