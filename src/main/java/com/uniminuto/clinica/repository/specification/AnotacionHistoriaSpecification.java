package com.uniminuto.clinica.repository.specification;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.FiltroPaginadoRq;
import com.uniminuto.clinica.util.ValidacionUtil;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros dinámicos del histórico de anotaciones.
 */
public final class AnotacionHistoriaSpecification {

    /**
     * Constructor privado: clase de utilidades estáticas.
     */
    private AnotacionHistoriaSpecification() {
    }

    /**
     * Construye el filtro de anotaciones. Se debe indicar la historia o la
     * mascota cuyas anotaciones se quieren consultar.
     *
     * @param historiaId historia clínica (opcional).
     * @param mascotaId  mascota, para ver el histórico completo (opcional).
     * @param filtro     filtros del listado (texto, fechas, veterinario).
     * @return especificación para el repositorio.
     */
    public static Specification<AnotacionHistoria> conFiltros(Long historiaId, Long mascotaId,
                                                              FiltroPaginadoRq filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            Path<Object> historia = root.get("historiaMedica");
            Path<Object> medico = root.get("medico");

            if (historiaId != null) {
                predicados.add(cb.equal(historia.get("historiaId"), historiaId));
            }
            if (mascotaId != null) {
                predicados.add(cb.equal(historia.get("mascota").get("mascotaId"), mascotaId));
            }
            String patron = ValidacionUtil.patronBusqueda(filtro.getTexto());
            if (patron != null) {
                predicados.add(cb.or(
                        cb.like(cb.lower(root.get("descripcion")), patron),
                        cb.like(cb.lower(root.get("observacion")), patron),
                        cb.like(cb.lower(root.get("diagnosticoEvolucion")), patron),
                        cb.like(FiltroSpecUtil.nombreCompleto(cb, medico), patron)
                ));
            }
            if (filtro.getMedicoId() != null) {
                predicados.add(cb.equal(medico.get("id"), filtro.getMedicoId()));
            }
            FiltroSpecUtil.rangoFechaHora(predicados, cb, root.get("fechaAnotacion"),
                    filtro.getFechaDesde(), filtro.getFechaHasta());
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
