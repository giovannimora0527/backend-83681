package com.uniminuto.clinica.repository.specification;

import com.uniminuto.clinica.entity.EstadoHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.HistoriaMedicaFiltroRq;
import com.uniminuto.clinica.util.ValidacionUtil;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros dinámicos del listado de historias clínicas.
 */
public final class HistoriaMedicaSpecification {

    /**
     * Constructor privado: clase de utilidades estáticas.
     */
    private HistoriaMedicaSpecification() {
    }

    /**
     * Construye el filtro de historias clínicas según los parámetros recibidos.
     *
     * @param filtro filtros del listado (texto, fechas, veterinario).
     * @param estado estado ya convertido a enum (opcional).
     * @return especificación para el repositorio.
     */
    public static Specification<HistoriaMedica> conFiltros(HistoriaMedicaFiltroRq filtro, EstadoHistoria estado) {
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
                        cb.like(cb.lower(root.get("motivoConsulta")), patron),
                        cb.like(cb.lower(root.get("diagnostico")), patron)
                ));
            }
            if (estado != null) {
                predicados.add(cb.equal(root.get("estado"), estado));
            }
            if (filtro.getMedicoId() != null) {
                predicados.add(cb.equal(medico.get("id"), filtro.getMedicoId()));
            }
            FiltroSpecUtil.rangoFechaHora(predicados, cb, root.get("fechaConsulta"),
                    filtro.getFechaDesde(), filtro.getFechaHasta());
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
