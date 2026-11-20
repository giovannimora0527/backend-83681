package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.TarifaServicio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface TarifaServicioRepository extends JpaRepository<TarifaServicio, Long> {
    List<TarifaServicio> findByVigenteHastaIsNullOrderByNombreAsc();
    List<TarifaServicio> findByNombreOrderByVigenteDesdeDesc(String nombre);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TarifaServicio t where t.id = :id")
    Optional<TarifaServicio> findLockedById(@Param("id") Long id);
}
