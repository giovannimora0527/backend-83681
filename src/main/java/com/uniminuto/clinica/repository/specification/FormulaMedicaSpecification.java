package com.uniminuto.clinica.repository.specification;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.FormulaMedicaFiltroRq;
import com.uniminuto.clinica.util.ValidacionUtil;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros dinámicos del listado de fórmulas médicas.
 */
public final class FormulaMedicaSpecification {

    /**
     * Constructor privado: clase de utilidades estáticas.
     */
    private FormulaMedicaSpecification() {
    }

    /**
     * Construye el filtro de fórmulas médicas según los parámetros recibidos.
     *
     * @param filtro filtros del listado (texto, fechas de emisión, veterinario, activo).
     * @return especificación para el repositorio.
     */
    public static Specification<FormulaMedica> conFiltros(FormulaMedicaFiltroRq filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            Path<Object> mascota = root.get("mascota");
            Path<Object> cliente = mascota.get("cliente");
            Path<Object> medico = root.get("medico");

            String patron = ValidacionUtil.patronBusqueda(filtro.getTexto());
            if (patron != null) {
                predicados.add(cb.or(
                        cb.like(cb.lower(root.get("medicamento")), patron),
                        cb.like(cb.lower(mascota.get("nombreMascota")), patron),
                        cb.like(FiltroSpecUtil.nombreCompleto(cb, cliente), patron),
                        cb.like(cb.lower(cliente.get("numeroDocumento")), patron),
                        cb.like(FiltroSpecUtil.nombreCompleto(cb, medico), patron)
                ));
            }
            if (filtro.getActivo() != null) {
                predicados.add(cb.equal(root.get("activo"), filtro.getActivo()));
            }
            if (filtro.getMedicoId() != null) {
                predicados.add(cb.equal(medico.get("id"), filtro.getMedicoId()));
            }
            FiltroSpecUtil.rangoFecha(predicados, cb, root.get("fechaEmision"),
                    filtro.getFechaDesde(), filtro.getFechaHasta());
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
