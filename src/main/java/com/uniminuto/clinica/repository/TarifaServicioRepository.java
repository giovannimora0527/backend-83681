package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.TarifaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
public interface TarifaServicioRepository extends JpaRepository<TarifaServicio, Long> {
    java.util.List<TarifaServicio> findByActivoTrueOrderByNombreAsc();
    java.util.List<TarifaServicio> findAllByOrderByNombreAsc();

    @org.springframework.data.jpa.repository.Query("""
            select t from TarifaServicio t
            where t.medico.id = :medicoId and t.activo = true
              and t.vigenciaDesde <= :fecha and (t.vigenciaHasta is null or t.vigenciaHasta >= :fecha)
            order by t.vigenciaDesde desc, t.id desc
            """)
    java.util.List<TarifaServicio> vigentesPorMedico(@Param("medicoId") Long medicoId, @Param("fecha") LocalDate fecha);

    @org.springframework.data.jpa.repository.Query("""
            select t from TarifaServicio t
            where t.especializacion.id = :especializacionId and t.medico is null and t.activo = true
              and t.vigenciaDesde <= :fecha and (t.vigenciaHasta is null or t.vigenciaHasta >= :fecha)
            order by t.vigenciaDesde desc, t.id desc
            """)
    java.util.List<TarifaServicio> vigentesPorEspecializacion(@Param("especializacionId") Long especializacionId,
            @Param("fecha") LocalDate fecha);
}
