package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.models.TarifaRs;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Consulta las tarifas aplicables al médico y registra los cambios de precio, vigencia e IVA. */
@Repository
@RequiredArgsConstructor
public class TarifaRepository {
    private static final String COLUMNAS = """
            SELECT t.id, t.especializacion_id, t.nombre_servicio, t.valor_base,
                   t.iva_porcentaje, t.activo, t.fecha_vigencia
            FROM tarifa t
            """;
    private static final String VIGENTES_POR_MEDICO = COLUMNAS + """
            JOIN medico m ON m.id = ?
            LEFT JOIN especializacion e ON e.id = t.especializacion_id
            WHERE t.activo = 1 AND t.fecha_vigencia <= CURRENT_DATE()
              AND NOT EXISTS (
                SELECT 1 FROM tarifa nueva
                WHERE nueva.especializacion_id <=> t.especializacion_id
                  AND REGEXP_REPLACE(TRIM(nueva.nombre_servicio), '[[:space:]]+', ' ') COLLATE utf8mb4_0900_ai_ci
                      = REGEXP_REPLACE(TRIM(t.nombre_servicio), '[[:space:]]+', ' ') COLLATE utf8mb4_0900_ai_ci
                  AND nueva.activo = 1 AND nueva.fecha_vigencia <= CURRENT_DATE()
                  AND (nueva.fecha_vigencia > t.fecha_vigencia
                       OR (nueva.fecha_vigencia = t.fecha_vigencia AND nueva.id > t.id))
              )
              AND (t.especializacion_id IS NULL OR (t.especializacion_id = m.especializacion_id AND e.activo = 1))
            """;
    private static final String ORDEN = " ORDER BY t.nombre_servicio, t.fecha_vigencia DESC, t.id";
    private static final RowMapper<TarifaRs> TARIFA = (rs, fila) -> new TarifaRs(
            rs.getLong("id"), rs.getObject("especializacion_id", Long.class), rs.getString("nombre_servicio"),
            rs.getBigDecimal("valor_base"), rs.getBigDecimal("iva_porcentaje"),
            rs.getBoolean("activo"), rs.getObject("fecha_vigencia", LocalDate.class));

    private final JdbcTemplate jdbc;

    /** Consulta los registros de la tarifa veterinaria. */
    public List<TarifaRs> listar(Long especializacionId) {
        return jdbc.query(COLUMNAS + " WHERE (? IS NULL OR t.especializacion_id = ?)" + ORDEN,
                TARIFA, especializacionId, especializacionId);
    }

    /** Consulta las tarifas vigentes para el veterinario. */
    public List<TarifaRs> listarVigentesParaMedico(long medicoId) {
        return jdbc.query(VIGENTES_POR_MEDICO + ORDEN, TARIFA, medicoId);
    }

    /** Consulta las tarifas vigentes aplicables al veterinario. */
    public Optional<TarifaRs> buscarVigenteParaMedico(long medicoId, long tarifaId) {
        return jdbc.query(VIGENTES_POR_MEDICO + " AND t.id = ?", TARIFA, medicoId, tarifaId)
                .stream().findFirst();
    }

    /** Obtiene el registro por su identificador y lo bloquea durante la operación. */
    public Optional<TarifaRs> bloquearPorId(long id) {
        return jdbc.query(COLUMNAS + " WHERE t.id = ? FOR UPDATE", TARIFA, id).stream().findFirst();
    }

    /** Guarda un nuevo registro con los valores recibidos. */
    public long insertar(TarifaRs tarifa) {
        var valores = new MapSqlParameterSource()
                .addValue("especializacion_id", tarifa.especializacionId(), Types.BIGINT)
                .addValue("nombre_servicio", tarifa.nombreServicio())
                .addValue("valor_base", tarifa.valorBase())
                .addValue("iva_porcentaje", tarifa.ivaPorcentaje())
                .addValue("activo", tarifa.activo())
                .addValue("fecha_vigencia", tarifa.fechaVigencia());
        return new SimpleJdbcInsert(jdbc).withTableName("tarifa")
                .usingColumns("especializacion_id", "nombre_servicio", "valor_base", "iva_porcentaje",
                        "activo", "fecha_vigencia").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(valores).longValue();
    }

    /** Guarda los cambios de la tarifa veterinaria. */
    public void actualizar(TarifaRs tarifa) {
        jdbc.update("""
                UPDATE tarifa SET especializacion_id = ?, nombre_servicio = ?, valor_base = ?,
                                  iva_porcentaje = ?, activo = ?, fecha_vigencia = ? WHERE id = ?
                """, tarifa.especializacionId(), tarifa.nombreServicio(), tarifa.valorBase(),
                tarifa.ivaPorcentaje(), tarifa.activo(), tarifa.fechaVigencia(), tarifa.id());
    }

    /** Consulta el historial de la tarifa veterinaria. */
    public List<Map<String, Object>> historial(long id) {
        return jdbc.queryForList("""
                SELECT id, campo_modificado AS campo, valor_anterior AS valorAnterior, valor_nuevo AS valorNuevo,
                       usuario_id AS usuarioId, fecha_cambio AS fechaCambio
                FROM tarifa_log WHERE tarifa_id = ? ORDER BY fecha_cambio DESC, id DESC
                """, id);
    }

    /** Guarda en el historial el campo modificado y sus valores anterior y nuevo. */
    public void registrar(long id, String campo, String anterior, String nuevo, long usuarioId) {
        jdbc.update("""
                INSERT INTO tarifa_log (tarifa_id, campo_modificado, valor_anterior, valor_nuevo, usuario_id)
                VALUES (?, ?, ?, ?, ?)
                """, id, campo, anterior, nuevo, usuarioId);
    }

    /** Comprueba que exista el producto indicado en el inventario. */
    public boolean existeMedicamento(long id) {
        return !jdbc.queryForList("SELECT id FROM medicamento WHERE id = ?", Long.class, id).isEmpty();
    }

    /** Devuelve el porcentaje de IVA del medicamento indicado. */
    public Optional<BigDecimal> consultarIvaMedicamento(long id) {
        return jdbc.query("SELECT iva_porcentaje FROM impuesto_medicamento WHERE medicamento_id = ?",
                (rs, fila) -> rs.getBigDecimal("iva_porcentaje"), id).stream().findFirst();
    }

    /** Guarda el porcentaje de IVA asignado al medicamento. */
    public void configurarIvaMedicamento(long id, BigDecimal porcentaje, long usuarioId) {
        jdbc.update("""
                INSERT INTO impuesto_medicamento (medicamento_id, iva_porcentaje, usuario_id)
                VALUES (?, ?, ?) AS configuracion
                ON DUPLICATE KEY UPDATE iva_porcentaje = configuracion.iva_porcentaje,
                    usuario_id = configuracion.usuario_id, fecha_configuracion = CURRENT_TIMESTAMP
                """, id, porcentaje, usuarioId);
    }
}
