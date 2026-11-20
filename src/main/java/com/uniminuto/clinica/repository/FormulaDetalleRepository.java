package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.FormulaDetalle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface FormulaDetalleRepository extends JpaRepository<FormulaDetalle, Long> {
    java.util.List<FormulaDetalle> findByAtencionIdOrderByIdAsc(Long atencionId);
    @Query("select f from FormulaDetalle f, AtencionMedica a, Cita c where a.id=f.atencionId and c.id=a.citaId " +
            "and f.cantidadDispensada < f.cantidadPrescrita and (:formulaId is null or f.id=:formulaId) " +
            "and (:mascotaId is null or c.mascota.mascotaId=:mascotaId) " +
            "and (:propietario is null or lower(c.cliente.nombres) like lower(concat('%',:propietario,'%')) " +
            "or lower(c.cliente.apellidos) like lower(concat('%',:propietario,'%')) " +
            "or lower(c.cliente.numeroDocumento) like lower(concat('%',:propietario,'%'))) " +
            "and (:desde is null or c.fechaHora>=:desde) and (:hasta is null or c.fechaHora<=:hasta) order by c.fechaHora asc, f.id asc")
    java.util.List<FormulaDetalle> buscarPendientes(@Param("formulaId") Long formulaId,@Param("mascotaId") Integer mascotaId,
            @Param("propietario") String propietario,@Param("desde") java.time.LocalDateTime desde,@Param("hasta") java.time.LocalDateTime hasta);
    @Query("select f from FormulaDetalle f where f.cantidadDispensada < f.cantidadPrescrita order by f.id asc")
    java.util.List<FormulaDetalle> findPendientesDeDispensar();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FormulaDetalle f where f.id = :id")
    Optional<FormulaDetalle> findLockedById(@Param("id") Long id);
}
