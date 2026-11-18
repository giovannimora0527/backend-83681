package com.uniminuto.clinica.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cuenta las citas por fecha programada y las consultas por fecha de atención. Para el total de
 * mascotas atendidas, cuenta a cada paciente una vez.
 */
@Repository
@RequiredArgsConstructor
public class ReporteMedicoRepository {

    private final JdbcTemplate jdbc;

    /**
     * Usa la fecha programada para la agenda y la de cierre para las atenciones realizadas. En
     * citas históricas sin cierre registrado usa la fecha programada.
     */
    public List<List<Object>> consultar(LocalDateTime desde, LocalDateTime hasta,
                                        Long medicoId, Long especializacionId) {
        return jdbc.query("""
                WITH citas_periodo AS (
                    SELECT m.id AS medico_id, m.nombres, m.apellidos, e.id AS especialidad_id,
                           e.nombre AS especialidad, c.estado, c.mascota_id,
                           (c.fecha_hora >= ? AND c.fecha_hora < ?) AS en_agenda,
                           (c.estado = 'ATENDIDA' AND COALESCE(c.fecha_atencion, c.fecha_hora) >= ?
                            AND COALESCE(c.fecha_atencion, c.fecha_hora) < ?) AS en_atencion
                    FROM cita c JOIN medico m ON m.id = c.medico_id
                    LEFT JOIN factura pf ON pf.cita_id = c.id AND pf.tipo_documento = 'PREFACTURA'
                    JOIN especializacion e ON e.id = COALESCE(pf.especializacion_emision_id, m.especializacion_id)
                    WHERE (? IS NULL OR m.id = ?) AND (? IS NULL OR e.id = ?)
                )
                SELECT medico_id, COALESCE(CONCAT(nombres, ' ', apellidos), 'TOTAL'),
                       especialidad_id, COALESCE(especialidad, ''),
                       SUM(en_agenda),
                       SUM(en_agenda AND estado = 'ATENDIDA'),
                       SUM(en_agenda AND estado = 'CANCELADA'),
                       SUM(en_agenda AND estado = 'PROGRAMADA'),
                       COUNT(DISTINCT CASE WHEN en_atencion THEN mascota_id END),
                       SUM(en_agenda AND estado = 'CONFIRMADA'),
                       SUM(en_agenda AND estado = 'NO_ASISTIDA'),
                       SUM(en_atencion)
                FROM citas_periodo WHERE en_agenda OR en_atencion
                GROUP BY medico_id, nombres, apellidos, especialidad_id, especialidad WITH ROLLUP
                HAVING GROUPING(especialidad) = 0 OR GROUPING(medico_id) = 1
                """, (rs, index) -> List.<Object>of(rs.getLong(1), rs.getString(2), rs.getLong(3),
                rs.getString(4), rs.getLong(5), rs.getLong(6), rs.getLong(7), rs.getLong(8),
                rs.getLong(9), rs.getLong(10), rs.getLong(11), rs.getLong(12)),
                desde, hasta, desde, hasta, medicoId, medicoId, especializacionId, especializacionId);
    }
}
