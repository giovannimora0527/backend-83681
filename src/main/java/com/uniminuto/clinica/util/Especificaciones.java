package com.uniminuto.clinica.util;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.Filtros;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Construye las consultas dinámicas de los listados paginados a partir de los filtros.
 * Un filtro nulo o vacío no restringe el resultado.
 */
public final class Especificaciones {

    private Especificaciones() {
    }

    /**
     * Especificación de citas.
     *
     * @param f filtros recibidos desde el cliente.
     * @return especificación lista para el repositorio.
     */
    public static Specification<Cita> cita(Filtros.Cita f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f != null) {
                if (f.clienteId() != null) {
                    p.add(cb.equal(root.get("cliente").get("clienteId"), f.clienteId()));
                }
                if (f.mascotaId() != null) {
                    p.add(cb.equal(root.get("mascota").get("mascotaId"), f.mascotaId()));
                }
                if (f.medicoId() != null) {
                    p.add(cb.equal(root.get("medico").get("id"), f.medicoId()));
                }
                if (hayTexto(f.estado())) {
                    p.add(cb.equal(root.<String>get("estado"), f.estado().trim()));
                }
                rangoFechaHora(cb, root.<LocalDateTime>get("fechaHora"), f.fechaDesde(), f.fechaHasta(), p);
                if (hayTexto(f.texto())) {
                    p.add(contiene(cb, root.<String>get("motivo"), f.texto()));
                }
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    /**
     * Especificación de fórmulas médicas.
     *
     * @param f filtros recibidos desde el cliente.
     * @return especificación lista para el repositorio.
     */
    public static Specification<FormulaMedica> formula(Filtros.Formula f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f != null) {
                if (f.mascotaId() != null) {
                    p.add(cb.equal(root.get("mascota").get("mascotaId"), f.mascotaId()));
                }
                if (f.medicoId() != null) {
                    p.add(cb.equal(root.get("medico").get("id"), f.medicoId()));
                }
                if (hayTexto(f.medicamento())) {
                    p.add(contiene(cb, root.get("medicamento").<String>get("nombre"), f.medicamento()));
                }
                if (f.fechaDesde() != null) {
                    p.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("fechaEmision"), f.fechaDesde()));
                }
                if (f.fechaHasta() != null) {
                    p.add(cb.lessThanOrEqualTo(root.<LocalDate>get("fechaEmision"), f.fechaHasta()));
                }
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    /**
     * Especificación de historias clínicas.
     *
     * @param f filtros recibidos desde el cliente.
     * @return especificación lista para el repositorio.
     */
    public static Specification<HistoriaMedica> historia(Filtros.Historia f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f != null) {
                if (f.clienteId() != null) {
                    p.add(cb.equal(root.get("cliente").get("clienteId"), f.clienteId()));
                }
                if (f.mascotaId() != null) {
                    p.add(cb.equal(root.get("mascota").get("mascotaId"), f.mascotaId()));
                }
                if (f.medicoId() != null) {
                    p.add(cb.equal(root.get("medico").get("id"), f.medicoId()));
                }
                rangoFechaHora(cb, root.<LocalDateTime>get("fechaHora"), f.fechaDesde(), f.fechaHasta(), p);
                if (hayTexto(f.texto())) {
                    p.add(cb.or(contiene(cb, root.<String>get("motivo"), f.texto()),
                            contiene(cb, root.<String>get("diagnostico"), f.texto())));
                }
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    /**
     * Especificación de anotaciones de una historia clínica.
     *
     * @param f filtros recibidos desde el cliente.
     * @return especificación lista para el repositorio.
     */
    public static Specification<AnotacionHistoria> anotacion(Filtros.Anotacion f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f != null) {
                if (f.historiaId() != null) {
                    p.add(cb.equal(root.get("historia").get("id"), f.historiaId()));
                }
                rangoFechaHora(cb, root.<LocalDateTime>get("fechaHora"), f.fechaDesde(), f.fechaHasta(), p);
                if (hayTexto(f.texto())) {
                    p.add(contiene(cb, root.<String>get("descripcion"), f.texto()));
                }
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static void rangoFechaHora(CriteriaBuilder cb, Path<LocalDateTime> campo,
                                       LocalDate desde, LocalDate hasta, List<Predicate> p) {
        if (desde != null) {
            p.add(cb.greaterThanOrEqualTo(campo, desde.atStartOfDay()));
        }
        if (hasta != null) {
            p.add(cb.lessThan(campo, hasta.plusDays(1).atStartOfDay()));
        }
    }

    private static Predicate contiene(CriteriaBuilder cb, Path<String> campo, String texto) {
        return cb.like(cb.lower(campo), "%" + texto.trim().toLowerCase() + "%");
    }

    private static boolean hayTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
