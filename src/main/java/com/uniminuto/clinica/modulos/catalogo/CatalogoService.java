package com.uniminuto.clinica.modulos.catalogo;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Módulo administrativo independiente: especialidades veterinarias y tarifas de servicios.
 * Los valores se parametrizan en la base de datos (no en el código) y cada cambio de tarifa
 * queda registrado en tarifa_log.
 */
@Service
public class CatalogoService {

    private static final List<String> TIPOS = List.of("CONSULTA", "PROCEDIMIENTO", "SERVICIO");

    private final Db db;
    private final CurrentUser current;

    public CatalogoService(Db db, CurrentUser current) {
        this.db = db;
        this.current = current;
    }

    // ------------------------------------------------------------------ especialidades

    public List<Map<String, Object>> especialidades(boolean soloActivas) {
        return db.list("SELECT id, nombre, descripcion, codigo_especializacion, activo FROM especializacion "
                + (soloActivas ? "WHERE activo = 1 " : "") + "ORDER BY nombre");
    }

    @Transactional
    public Map<String, Object> crearEspecialidad(Map<String, Object> b) {
        String nombre = In.req(b, "nombre", "Nombre");
        String codigo = In.req(b, "codigoEspecializacion", "Código");
        validarEspecialidad(nombre, codigo, -1L);
        long id = db.insert("INSERT INTO especializacion (nombre, descripcion, codigo_especializacion, activo) VALUES (?,?,?,1)",
                nombre, In.str(b, "descripcion"), codigo);
        return especialidad(id);
    }

    @Transactional
    public Map<String, Object> editarEspecialidad(long id, Map<String, Object> b) {
        especialidad(id);
        String nombre = In.req(b, "nombre", "Nombre");
        String codigo = In.req(b, "codigoEspecializacion", "Código");
        validarEspecialidad(nombre, codigo, id);
        db.update("UPDATE especializacion SET nombre = ?, descripcion = ?, codigo_especializacion = ? WHERE id = ?",
                nombre, In.str(b, "descripcion"), codigo, id);
        return especialidad(id);
    }

    @Transactional
    public Map<String, Object> estadoEspecialidad(long id, boolean activo) {
        especialidad(id);
        db.update("UPDATE especializacion SET activo = ? WHERE id = ?", activo, id);
        return especialidad(id);
    }

    private Map<String, Object> especialidad(long id) {
        return db.must("La especialidad no existe.",
                "SELECT id, nombre, descripcion, codigo_especializacion, activo FROM especializacion WHERE id = ?", id);
    }

    private void validarEspecialidad(String nombre, String codigo, long idExcluir) {
        if (nombre.length() > 100 || codigo.length() > 10) {
            throw new BadRequestException("El nombre admite hasta 100 caracteres y el código hasta 10.");
        }
        if (db.count("SELECT COUNT(*) FROM especializacion WHERE nombre = ? AND id <> ?", nombre, idExcluir) > 0) {
            throw new BadRequestException("Ya existe una especialidad con el nombre '" + nombre + "'.");
        }
        if (db.count("SELECT COUNT(*) FROM especializacion WHERE codigo_especializacion = ? AND id <> ?", codigo, idExcluir) > 0) {
            throw new BadRequestException("Ya existe una especialidad con el código '" + codigo + "'.");
        }
    }

    // ------------------------------------------------------------------ tarifas

    public List<Map<String, Object>> tarifas(String tipo, Boolean activo, Long especialidadId) {
        StringBuilder sql = new StringBuilder("""
                SELECT t.id, t.codigo, t.nombre, t.tipo, t.especializacion_id, e.nombre AS especialidad, t.valor,
                       t.iva_porcentaje, ROUND(t.valor * (1 + t.iva_porcentaje / 100), 2) AS valor_con_iva,
                       t.activo, t.fecha_creacion, t.fecha_modificacion
                  FROM tarifa t LEFT JOIN especializacion e ON e.id = t.especializacion_id WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND t.tipo = ?");
            args.add(tipo.trim().toUpperCase(Locale.ROOT));
        }
        if (activo != null) {
            sql.append(" AND t.activo = ?");
            args.add(activo);
        }
        if (especialidadId != null) {
            sql.append(" AND (t.especializacion_id = ? OR t.especializacion_id IS NULL)");
            args.add(especialidadId);
        }
        sql.append(" ORDER BY t.tipo, t.nombre");
        return db.list(sql.toString(), args.toArray());
    }

    public Map<String, Object> tarifa(long id) {
        return db.must("La tarifa no existe.", """
                SELECT t.id, t.codigo, t.nombre, t.tipo, t.especializacion_id, e.nombre AS especialidad, t.valor,
                       t.iva_porcentaje, t.activo, t.fecha_creacion, t.fecha_modificacion
                  FROM tarifa t LEFT JOIN especializacion e ON e.id = t.especializacion_id WHERE t.id = ?
                """, id);
    }

    @Transactional
    public Map<String, Object> crearTarifa(Map<String, Object> b) {
        Valores v = validarTarifa(b, -1L);
        long id = db.insert("""
                INSERT INTO tarifa (codigo, nombre, tipo, especializacion_id, valor, iva_porcentaje, activo)
                VALUES (?,?,?,?,?,?,1)
                """, v.codigo(), v.nombre(), v.tipo(), v.especialidadId(), v.valor(), v.iva());
        registrarLog(id, null, v.valor(), null, v.iva(), "Creación de la tarifa");
        return tarifa(id);
    }

    @Transactional
    public Map<String, Object> editarTarifa(long id, Map<String, Object> b) {
        Map<String, Object> actual = tarifa(id);
        Valores v = validarTarifa(b, id);
        BigDecimal valorAnt = In.toDec(actual.get("valor"));
        BigDecimal ivaAnt = In.toDec(actual.get("ivaPorcentaje"));
        db.update("""
                UPDATE tarifa SET codigo = ?, nombre = ?, tipo = ?, especializacion_id = ?, valor = ?, iva_porcentaje = ?,
                                  fecha_modificacion = NOW()
                 WHERE id = ?
                """, v.codigo(), v.nombre(), v.tipo(), v.especialidadId(), v.valor(), v.iva(), id);
        // Historial de cambios de tarifa
        if (valorAnt.compareTo(v.valor()) != 0 || ivaAnt.compareTo(v.iva()) != 0) {
            registrarLog(id, valorAnt, v.valor(), ivaAnt, v.iva(), In.str(b, "motivo"));
        }
        return tarifa(id);
    }

    @Transactional
    public Map<String, Object> estadoTarifa(long id, boolean activo) {
        tarifa(id);
        db.update("UPDATE tarifa SET activo = ?, fecha_modificacion = NOW() WHERE id = ?", activo, id);
        return tarifa(id);
    }

    /** Historial de cambios de tarifas (de una tarifa o de todas). */
    public List<Map<String, Object>> logTarifas(Long tarifaId) {
        String sql = """
                SELECT l.id, l.tarifa_id, t.codigo, t.nombre, l.valor_anterior, l.valor_nuevo, l.iva_anterior,
                       l.iva_nuevo, l.fecha, l.motivo, u.username AS usuario
                  FROM tarifa_log l JOIN tarifa t ON t.id = l.tarifa_id JOIN usuario u ON u.id = l.usuario_id
                """;
        if (tarifaId != null) {
            return db.list(sql + " WHERE l.tarifa_id = ? ORDER BY l.id DESC", tarifaId);
        }
        return db.list(sql + " ORDER BY l.id DESC");
    }

    private void registrarLog(long tarifaId, BigDecimal valorAnt, BigDecimal valorNuevo, BigDecimal ivaAnt,
                              BigDecimal ivaNuevo, String motivo) {
        db.update("""
                INSERT INTO tarifa_log (tarifa_id, valor_anterior, valor_nuevo, iva_anterior, iva_nuevo, usuario_id, motivo)
                VALUES (?,?,?,?,?,?,?)
                """, tarifaId, valorAnt, valorNuevo, ivaAnt, ivaNuevo, current.get().id(), motivo);
    }

    private record Valores(String codigo, String nombre, String tipo, Long especialidadId, BigDecimal valor, BigDecimal iva) {
    }

    private Valores validarTarifa(Map<String, Object> b, long idExcluir) {
        String codigo = In.req(b, "codigo", "Código").toUpperCase(Locale.ROOT);
        String nombre = In.req(b, "nombre", "Nombre");
        String tipo = In.req(b, "tipo", "Tipo").toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(tipo)) {
            throw new BadRequestException("El tipo de tarifa debe ser CONSULTA, PROCEDIMIENTO o SERVICIO.");
        }
        if (codigo.length() > 20 || nombre.length() > 150) {
            throw new BadRequestException("El código admite hasta 20 caracteres y el nombre hasta 150.");
        }
        BigDecimal valor = In.reqDec(b, "valor", "Valor");
        BigDecimal iva = In.dec(b, "ivaPorcentaje") == null ? BigDecimal.ZERO : In.dec(b, "ivaPorcentaje");
        if (valor.signum() < 0) {
            throw new BadRequestException("El valor de la tarifa no puede ser negativo.");
        }
        if (iva.signum() < 0 || iva.compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("El IVA debe estar entre 0 y 100.");
        }
        Long esp = In.lng(b, "especializacionId");
        if (esp != null) {
            Map<String, Object> e = especialidad(esp);
            if (!Boolean.TRUE.equals(e.get("activo"))) {
                throw new BadRequestException("La especialidad seleccionada está inactiva.");
            }
        }
        if (db.count("SELECT COUNT(*) FROM tarifa WHERE codigo = ? AND id <> ?", codigo, idExcluir) > 0) {
            throw new BadRequestException("Ya existe una tarifa con el código '" + codigo + "'.");
        }
        return new Valores(codigo, nombre, tipo, esp, valor, iva);
    }
}
