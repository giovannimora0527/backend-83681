package com.uniminuto.clinica.repository.specification;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.util.ValidacionUtil;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros dinámicos del listado de citas.
 */
public final class CitaSpecification {

    /**
     * Constructor privado: clase de utilidades estáticas.
     */
    private CitaSpecification() {
    }

    /**
     * Construye el filtro de citas según los parámetros recibidos. Los
     * parámetros vacíos no se aplican.
     *
     * @param filtro filtros del listado (texto, fechas, veterinario).
     * @param estado estado ya convertido a enum (opcional).
     * @return especificación para el repositorio.
     */
    public static Specification<Cita> conFiltros(CitaFiltroRq filtro, EstadoCita estado) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            Path<Object> mascota = root.get("mascota");
            Path<Object> cliente = mascota.get("cliente");
            Path<Object> medico = root.get("medico");

            String patron = ValidacionUtil.patronBusqueda(filtro.getTexto());
            if (patron != null) {
                predicados.add(cb.or(
                        cb.like(cb.lower(mascota.get("nombreMascota")), patron),
                        cb.like(FiltroSpecUtil.nombreCompleto(cb, cliente), patron),
                        cb.like(cb.lower(cliente.get("numeroDocumento")), patron),
                        cb.like(FiltroSpecUtil.nombreCompleto(cb, medico), patron),
                        cb.like(cb.lower(root.get("motivo")), patron)
                ));
            }
            if (estado != null) {
                predicados.add(cb.equal(root.get("estado"), estado));
            }
            if (filtro.getMedicoId() != null) {
                predicados.add(cb.equal(medico.get("id"), filtro.getMedicoId()));
            }
            FiltroSpecUtil.rangoFechaHora(predicados, cb, root.get("fechaCita"),
                    filtro.getFechaDesde(), filtro.getFechaHasta());
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
