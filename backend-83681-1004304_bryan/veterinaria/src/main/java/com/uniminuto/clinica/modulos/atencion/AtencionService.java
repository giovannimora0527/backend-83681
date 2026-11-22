package com.uniminuto.clinica.modulos.atencion;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import com.uniminuto.clinica.support.PdfKit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Atención médica de la cita: motivo, síntomas, diagnóstico, observaciones, recomendaciones,
 * servicios (tarifas) prestados y fórmula médica. La fórmula NO descuenta inventario:
 * el descuento ocurre solo en el despacho.
 */
@Service
public class AtencionService {

    private static final String SELECT_ATENCION = """
            SELECT a.id, a.cita_id, a.historia_id, a.medico_id, a.mascota_id, a.motivo_consulta, a.sintomas,
                   a.diagnostico, a.observaciones, a.recomendaciones, a.estado, a.fecha_inicio, a.fecha_fin,
                   c.fecha_hora AS cita_inicio, c.estado AS cita_estado, c.cliente_id,
                   ma.nombre_mascota, ma.edad AS mascota_edad, r.nombre AS raza, r.especie,
                   CONCAT(cl.nombres,' ',cl.apellidos) AS propietario, cl.telefono AS propietario_telefono,
                   cl.numero_documento AS propietario_documento,
                   CONCAT(me.nombres,' ',me.apellidos) AS medico_nombre, es.nombre AS especialidad,
                   me.especializacion_id
              FROM atencion_medica a
              JOIN cita c ON c.id = a.cita_id
              JOIN mascota ma ON ma.mascota_id = a.mascota_id
              JOIN raza r ON r.raza_id = ma.raza_id
              JOIN cliente cl ON cl.cliente_id = c.cliente_id
              JOIN medico me ON me.id = a.medico_id
              JOIN especializacion es ON es.id = me.especializacion_id
            """;

    private final Db db;
    private final CurrentUser current;

    public AtencionService(Db db, CurrentUser current) {
        this.db = db;
        this.current = current;
    }

    // ------------------------------------------------------------------ consulta

    /** Atenciones (el MEDICO solo ve las propias). */
    public List<Map<String, Object>> listar(String estado, String q) {
        StringBuilder sql = new StringBuilder(SELECT_ATENCION).append(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        Long medicoId = current.get().is("MEDICO") ? current.medicoId() : null;
        if (current.get().is("MEDICO") && medicoId == null) {
            return List.of();
        }
        if (medicoId != null) {
            sql.append(" AND a.medico_id = ?");
            args.add(medicoId);
        }
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND a.estado = ?");
            args.add(estado.trim().toUpperCase());
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (ma.nombre_mascota LIKE ? OR cl.nombres LIKE ? OR cl.apellidos LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY a.fecha_inicio DESC LIMIT 300");
        return db.list(sql.toString(), args.toArray());
    }

    public Map<String, Object> detalle(long id) {
        Map<String, Object> a = cargar(id);
        a.put("servicios", servicios(id));
        a.put("formula", formulaDe(id));
        a.put("totales", totales(id));
        a.put("antecedentes", antecedentes(In.toInt(a.get("mascotaId")), id));
        return a;
    }

    public Map<String, Object> porCita(long citaId) {
        Long id = db.scalar(Long.class, "SELECT id FROM atencion_medica WHERE cita_id = ?", citaId);
        if (id == null) {
            throw new BadRequestException("La cita aún no tiene una atención iniciada.", org.springframework.http.HttpStatus.NOT_FOUND);
        }
        return detalle(id);
    }

    /** Historial clínico previo de la mascota: atenciones finalizadas y anotaciones de la historia. */
    private Map<String, Object> antecedentes(int mascotaId, long excluirAtencion) {
        List<Map<String, Object>> atenciones = db.list("""
                SELECT a.id, a.fecha_inicio, a.motivo_consulta, a.diagnostico, a.recomendaciones,
                       CONCAT(me.nombres,' ',me.apellidos) AS medico
                  FROM atencion_medica a JOIN medico me ON me.id = a.medico_id
                 WHERE a.mascota_id = ? AND a.id <> ? AND a.estado = 'FINALIZADA'
                 ORDER BY a.fecha_inicio DESC LIMIT 20
                """, mascotaId, excluirAtencion);
        List<Map<String, Object>> formulas = db.list("""
                SELECT f.id AS formula_id, f.fecha_creacion_registro AS fecha, m.nombre AS medicamento,
                       d.dosis, d.frecuencia, d.duracion_dias, d.cantidad_prescrita
                  FROM formula_medica f
                  JOIN formula_medica_detalle d ON d.formula_id = f.id
                  JOIN medicamento m ON m.id = d.medicamento_id
                 WHERE f.mascota_id = ? AND f.estado = 'CONFIRMADA'
                 ORDER BY f.fecha_creacion_registro DESC, d.id LIMIT 40
                """, mascotaId);
        return Map.of("atenciones", atenciones, "formulas", formulas);
    }

    // ------------------------------------------------------------------ flujo

    @Transactional
    public Map<String, Object> iniciar(long citaId) {
        Map<String, Object> cita = db.must("La cita no existe.", """
                SELECT c.id, c.mascota_id, c.medico_id, c.estado, c.motivo, me.especializacion_id
                  FROM cita c JOIN medico me ON me.id = c.medico_id WHERE c.id = ? FOR UPDATE
                """, citaId);
        verificarMedico(In.toLong(cita.get("medicoId")));
        Long existente = db.scalar(Long.class, "SELECT id FROM atencion_medica WHERE cita_id = ?", citaId);
        if (existente != null) {
            return detalle(existente);
        }
        String estado = String.valueOf(cita.get("estado"));
        if (!estado.equals("PENDIENTE") && !estado.equals("CONFIRMADA")) {
            throw new BadRequestException("Solo se puede iniciar la atención de una cita pendiente o confirmada (estado actual: "
                    + estado + ").");
        }
        int mascotaId = In.toInt(cita.get("mascotaId"));
        db.update("INSERT IGNORE INTO historia_medica (paciente_id) VALUES (?)", mascotaId);
        Long historiaId = db.scalar(Long.class, "SELECT id FROM historia_medica WHERE paciente_id = ?", mascotaId);
        String motivo = cita.get("motivo") == null || String.valueOf(cita.get("motivo")).isBlank()
                ? "Consulta veterinaria" : String.valueOf(cita.get("motivo"));
        long id = db.insert("""
                INSERT INTO atencion_medica (cita_id, historia_id, medico_id, mascota_id, motivo_consulta, estado)
                VALUES (?,?,?,?,?, 'EN_CURSO')
                """, citaId, historiaId, In.toLong(cita.get("medicoId")), mascotaId, motivo);
        db.update("UPDATE cita SET estado = 'EN_ATENCION', fecha_modificacion = NOW() WHERE id = ?", citaId);
        // Una fórmula migrada del modelo anterior (sin atención) se vincula a la nueva atención
        db.update("UPDATE formula_medica SET atencion_id = ? WHERE cita_id = ? AND atencion_id IS NULL", id, citaId);

        // Consulta base según la especialidad del veterinario (o la tarifa general), con valor histórico
        List<Map<String, Object>> base = db.list("""
                SELECT id FROM tarifa WHERE tipo = 'CONSULTA' AND activo = 1
                   AND (especializacion_id = ? OR especializacion_id IS NULL)
                 ORDER BY (especializacion_id IS NULL), id LIMIT 1
                """, In.toLong(cita.get("especializacionId")));
        if (!base.isEmpty()) {
            insertarServicio(id, In.toLong(base.get(0).get("id")), 1);
        }
        return detalle(id);
    }

    @Transactional
    public Map<String, Object> guardar(long id, Map<String, Object> b) {
        Map<String, Object> a = cargarEditable(id);
        String motivo = In.str(b, "motivoConsulta");
        if (motivo == null) {
            motivo = String.valueOf(a.get("motivoConsulta"));
        }
        db.update("""
                UPDATE atencion_medica SET motivo_consulta = ?, sintomas = ?, diagnostico = ?, observaciones = ?,
                                           recomendaciones = ?
                 WHERE id = ?
                """, motivo, In.str(b, "sintomas"), In.str(b, "diagnostico"), In.str(b, "observaciones"),
                In.str(b, "recomendaciones"), id);
        return detalle(id);
    }

    // ------------------------------------------------------------------ servicios

    public List<Map<String, Object>> servicios(long atencionId) {
        return db.list("""
                SELECT s.id, s.tarifa_id, t.codigo, t.tipo, s.descripcion, s.cantidad, s.valor_unitario, s.iva_porcentaje,
                       ROUND(s.cantidad * s.valor_unitario, 2) AS subtotal,
                       ROUND(s.cantidad * s.valor_unitario * s.iva_porcentaje / 100, 2) AS valor_iva,
                       ROUND(s.cantidad * s.valor_unitario * (1 + s.iva_porcentaje / 100), 2) AS total
                  FROM atencion_servicio s JOIN tarifa t ON t.id = s.tarifa_id
                 WHERE s.atencion_id = ? ORDER BY s.id
                """, atencionId);
    }

    @Transactional
    public Map<String, Object> agregarServicio(long id, Map<String, Object> b) {
        cargarEditable(id);
        long tarifaId = In.reqLng(b, "tarifaId", "Tarifa");
        Integer cant = In.intg(b, "cantidad");
        int cantidad = cant == null ? 1 : cant;
        if (cantidad < 1 || cantidad > 999) {
            throw new BadRequestException("La cantidad debe estar entre 1 y 999.");
        }
        Long existe = db.scalar(Long.class, "SELECT id FROM atencion_servicio WHERE atencion_id = ? AND tarifa_id = ?", id, tarifaId);
        if (existe != null) {
            db.update("UPDATE atencion_servicio SET cantidad = ? WHERE id = ?", cantidad, existe);
        } else {
            insertarServicio(id, tarifaId, cantidad);
        }
        return detalle(id);
    }

    @Transactional
    public Map<String, Object> quitarServicio(long id, long servicioId) {
        cargarEditable(id);
        if (db.update("DELETE FROM atencion_servicio WHERE id = ? AND atencion_id = ?", servicioId, id) == 0) {
            throw new BadRequestException("El servicio no pertenece a esta atención.");
        }
        return detalle(id);
    }

    private void insertarServicio(long atencionId, long tarifaId, int cantidad) {
        Map<String, Object> t = db.must("La tarifa no existe.",
                "SELECT id, nombre, valor, iva_porcentaje, activo FROM tarifa WHERE id = ?", tarifaId);
        if (!Boolean.TRUE.equals(t.get("activo")) && !"1".equals(String.valueOf(t.get("activo")))
                && !"true".equals(String.valueOf(t.get("activo")))) {
            throw new BadRequestException("La tarifa '" + t.get("nombre") + "' está inactiva.");
        }
        // Se copia el valor vigente: cambios posteriores de tarifa no alteran atenciones anteriores
        db.insert("""
                INSERT INTO atencion_servicio (atencion_id, tarifa_id, descripcion, cantidad, valor_unitario, iva_porcentaje)
                VALUES (?,?,?,?,?,?)
                """, atencionId, tarifaId, String.valueOf(t.get("nombre")), cantidad, t.get("valor"), t.get("ivaPorcentaje"));
    }

    private Map<String, Object> totales(long atencionId) {
        BigDecimal sub = BigDecimal.ZERO;
        BigDecimal iva = BigDecimal.ZERO;
        for (Map<String, Object> s : servicios(atencionId)) {
            sub = sub.add(In.toDec(s.get("subtotal")));
            iva = iva.add(In.toDec(s.get("valorIva")));
        }
        return Map.of("subtotal", sub, "iva", iva, "total", sub.add(iva).setScale(2, RoundingMode.HALF_UP));
    }

    // ------------------------------------------------------------------ fórmula

    public Map<String, Object> formulaDe(long atencionId) {
        Map<String, Object> f = db.one("""
                SELECT f.id, f.cita_id, f.estado, f.estado_despacho, ed.nombre AS estado_despacho_nombre, f.observaciones,
                       f.fecha_creacion_registro AS fecha_creacion, f.fecha_confirmacion
                  FROM formula_medica f JOIN estado_despacho ed ON ed.codigo = f.estado_despacho
                 WHERE f.atencion_id = ?
                """, atencionId);
        if (f == null) {
            return null;
        }
        f.put("items", itemsFormula(In.toLong(f.get("id"))));
        return f;
    }

    private List<Map<String, Object>> itemsFormula(long formulaId) {
        return db.list("""
                SELECT d.id, d.medicamento_id, m.codigo, m.nombre AS medicamento, m.tipo, d.dosis, d.frecuencia, d.duracion_dias,
                       d.cantidad_prescrita, d.cantidad_dispensada, d.via_administracion, d.indicaciones
                  FROM formula_medica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                 WHERE d.formula_id = ? ORDER BY d.id
                """, formulaId);
    }

    /**
     * Crea o reemplaza el borrador de la fórmula (varios medicamentos). No toca el inventario.
     * Cuerpo: {observaciones, items:[{medicamentoId, dosis, frecuencia, duracionDias, cantidadPrescrita,
     * viaAdministracion, indicaciones}]}
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> guardarFormula(long atencionId, Map<String, Object> b) {
        Map<String, Object> a = cargarEditable(atencionId);
        Object raw = b.get("items");
        if (!(raw instanceof List<?> lista)) {
            throw new BadRequestException("Indique la lista de medicamentos de la fórmula (items).");
        }
        Long formulaId = db.scalar(Long.class, "SELECT id FROM formula_medica WHERE cita_id = ? FOR UPDATE", In.toLong(a.get("citaId")));
        if (formulaId != null) {
            String ed = db.scalar(String.class, "SELECT estado_despacho FROM formula_medica WHERE id = ?", formulaId);
            if (!"PENDIENTE_DESPACHO".equals(ed)) {
                throw new BadRequestException("La fórmula ya está en proceso de despacho y no puede modificarse.");
            }
        }
        List<Map<String, Object>> items = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        int n = 0;
        for (Object o : lista) {
            n++;
            Map<String, Object> it = (Map<String, Object>) o;
            long medId = In.reqLng(it, "medicamentoId", "Medicamento (ítem " + n + ")");
            if (!vistos.add(medId)) {
                throw new BadRequestException("El medicamento está repetido en la fórmula (ítem " + n + ").");
            }
            Map<String, Object> med = db.must("El medicamento del ítem " + n + " no existe.",
                    "SELECT id, nombre, activo FROM medicamento WHERE id = ?", medId);
            if (!"1".equals(String.valueOf(med.get("activo"))) && !"true".equals(String.valueOf(med.get("activo")))) {
                throw new BadRequestException("El medicamento '" + med.get("nombre") + "' está inactivo.");
            }
            String dosis = In.req(it, "dosis", "Dosis (ítem " + n + ")");
            Integer cant = In.intg(it, "cantidadPrescrita");
            if (cant == null || cant < 1 || cant > 9999) {
                throw new BadRequestException("La cantidad prescrita del ítem " + n + " debe estar entre 1 y 9999.");
            }
            Integer dias = In.intg(it, "duracionDias");
            if (dias != null && (dias < 1 || dias > 365)) {
                throw new BadRequestException("La duración del ítem " + n + " debe estar entre 1 y 365 días.");
            }
            items.add(Map.of("med", medId, "dosis", dosis, "cant", cant,
                    "frec", In.str(it, "frecuencia") == null ? "" : In.str(it, "frecuencia"),
                    "dias", dias == null ? -1 : dias,
                    "via", In.str(it, "viaAdministracion") == null ? "" : In.str(it, "viaAdministracion"),
                    "ind", In.str(it, "indicaciones") == null ? "" : In.str(it, "indicaciones")));
        }
        if (items.isEmpty()) {
            if (formulaId != null) {
                db.update("DELETE FROM formula_medica_detalle WHERE formula_id = ?", formulaId);
                db.update("DELETE FROM formula_medica WHERE id = ?", formulaId);
            }
            return detalle(atencionId);
        }
        if (formulaId == null) {
            formulaId = db.insert("""
                    INSERT INTO formula_medica (cita_id, atencion_id, medico_id, mascota_id, estado, observaciones)
                    VALUES (?,?,?,?, 'BORRADOR', ?)
                    """, In.toLong(a.get("citaId")), atencionId, In.toLong(a.get("medicoId")), In.toInt(a.get("mascotaId")),
                    In.str(b, "observaciones"));
        } else {
            db.update("UPDATE formula_medica SET observaciones = ?, fecha_actualizacion_registro = NOW() WHERE id = ?",
                    In.str(b, "observaciones"), formulaId);
            db.update("DELETE FROM formula_medica_detalle WHERE formula_id = ?", formulaId);
        }
        for (Map<String, Object> it : items) {
            int dias = (Integer) it.get("dias");
            db.insert("""
                    INSERT INTO formula_medica_detalle (formula_id, medicamento_id, dosis, frecuencia, duracion_dias,
                                                        cantidad_prescrita, via_administracion, indicaciones)
                    VALUES (?,?,?,?,?,?,?,?)
                    """, formulaId, it.get("med"), it.get("dosis"), blank((String) it.get("frec")),
                    dias < 0 ? null : dias, it.get("cant"), blank((String) it.get("via")), blank((String) it.get("ind")));
        }
        return detalle(atencionId);
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /**
     * Finaliza la atención: exige diagnóstico, deja constancia en la historia clínica, la cita pasa a
     * ATENDIDA y la fórmula (si existe) queda confirmada y pendiente de despacho.
     */
    @Transactional
    public Map<String, Object> finalizar(long id) {
        Map<String, Object> a = cargarEditable(id);
        String diag = a.get("diagnostico") == null ? "" : String.valueOf(a.get("diagnostico")).trim();
        if (diag.isEmpty()) {
            throw new BadRequestException("Registre el diagnóstico antes de finalizar la atención.");
        }
        if (servicios(id).isEmpty()) {
            throw new BadRequestException("La atención debe tener al menos un servicio (consulta) para poder facturarse.");
        }
        long usuario = current.get().id();
        db.update("UPDATE atencion_medica SET estado = 'FINALIZADA', fecha_fin = NOW() WHERE id = ?", id);
        db.update("UPDATE cita SET estado = 'ATENDIDA', fecha_modificacion = NOW() WHERE id = ?", In.toLong(a.get("citaId")));

        StringBuilder nota = new StringBuilder("Motivo: ").append(a.get("motivoConsulta"));
        if (a.get("sintomas") != null) nota.append("\nSíntomas: ").append(a.get("sintomas"));
        nota.append("\nDiagnóstico: ").append(diag);
        if (a.get("observaciones") != null) nota.append("\nObservaciones: ").append(a.get("observaciones"));
        if (a.get("recomendaciones") != null) nota.append("\nRecomendaciones: ").append(a.get("recomendaciones"));
        db.insert("""
                INSERT INTO anotacion_historia (historia_id, medico_id, cita_id, descripcion, diagnostico, observaciones)
                VALUES (?,?,?,?,?,?)
                """, In.toLong(a.get("historiaId")), In.toLong(a.get("medicoId")), In.toLong(a.get("citaId")),
                nota.toString(), diag, a.get("observaciones"));

        Long formulaId = db.scalar(Long.class, "SELECT id FROM formula_medica WHERE atencion_id = ? FOR UPDATE", id);
        if (formulaId != null) {
            db.update("UPDATE formula_medica SET estado = 'CONFIRMADA', fecha_confirmacion = NOW() WHERE id = ?", formulaId);
            db.update("""
                    INSERT INTO despacho_estado_log (formula_id, estado_anterior, estado_nuevo, usuario_id, observacion)
                    VALUES (?, NULL, 'PENDIENTE_DESPACHO', ?, 'Fórmula confirmada al finalizar la atención')
                    """, formulaId, usuario);
        }
        return detalle(id);
    }

    // ------------------------------------------------------------------ PDFs

    public byte[] prefacturaPdf(long id) {
        Map<String, Object> a = cargar(id);
        List<Map<String, Object>> servs = servicios(id);
        Map<String, Object> tot = totales(id);
        Map<String, Object> clinica = db.one("SELECT * FROM clinica_config WHERE id = 1");
        return PdfKit.render(false, doc -> {
            PdfKit.header(doc, clinica, "PREFACTURA – Atención médica N.º " + id);
            cabeceraAtencion(doc, a);
            PdfKit.section(doc, "Servicios prestados");
            PdfPTable t = PdfKit.table(new float[]{4f, 1f, 1.6f, 1f, 1.6f, 1.8f},
                    "Descripción", "Cant.", "Valor unit.", "IVA %", "Valor IVA", "Total");
            for (Map<String, Object> s : servs) {
                PdfKit.cell(t, s.get("descripcion"), false);
                PdfKit.cell(t, s.get("cantidad"), true);
                PdfKit.cell(t, PdfKit.money(s.get("valorUnitario")), true);
                PdfKit.cell(t, s.get("ivaPorcentaje"), true);
                PdfKit.cell(t, PdfKit.money(s.get("valorIva")), true);
                PdfKit.cell(t, PdfKit.money(s.get("total")), true);
            }
            doc.add(t);
            PdfKit.line(doc, "Subtotal", PdfKit.money(tot.get("subtotal")));
            PdfKit.line(doc, "IVA", PdfKit.money(tot.get("iva")));
            PdfKit.line(doc, "TOTAL SERVICIOS", PdfKit.money(tot.get("total")));
            PdfKit.line(doc, "Nota", "Los medicamentos se facturan únicamente cuando son dispensados en el despacho. "
                    + "Este documento es una prefactura y no constituye factura de venta.");
            PdfKit.line(doc, "Generado", PdfKit.now());
        });
    }

    public byte[] formulaPdf(long atencionId) {
        Map<String, Object> a = cargar(atencionId);
        Map<String, Object> f = formulaDe(atencionId);
        if (f == null) {
            throw new BadRequestException("La atención no tiene fórmula médica.");
        }
        Map<String, Object> clinica = db.one("SELECT * FROM clinica_config WHERE id = 1");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) f.get("items");
        return PdfKit.render(true, doc -> {
            PdfKit.header(doc, clinica, "FÓRMULA MÉDICA N.º " + f.get("id")
                    + ("BORRADOR".equals(f.get("estado")) ? " (borrador)" : ""));
            cabeceraAtencion(doc, a);
            PdfKit.section(doc, "Medicamentos prescritos");
            PdfPTable t = PdfKit.table(new float[]{1.2f, 3f, 2.6f, 1.6f, 1.2f, 1f, 1.5f, 3f},
                    "Código", "Medicamento", "Dosis", "Frecuencia", "Duración (días)", "Cant.", "Vía", "Indicaciones");
            for (Map<String, Object> it : items) {
                PdfKit.cell(t, it.get("codigo"), false);
                PdfKit.cell(t, it.get("medicamento"), false);
                PdfKit.cell(t, it.get("dosis"), false);
                PdfKit.cell(t, PdfKit.nz(it.get("frecuencia")), false);
                PdfKit.cell(t, PdfKit.nz(it.get("duracionDias")), true);
                PdfKit.cell(t, it.get("cantidadPrescrita"), true);
                PdfKit.cell(t, PdfKit.nz(it.get("viaAdministracion")), false);
                PdfKit.cell(t, PdfKit.nz(it.get("indicaciones")), false);
            }
            doc.add(t);
            if (f.get("observaciones") != null) {
                PdfKit.line(doc, "Observaciones", String.valueOf(f.get("observaciones")));
            }
            PdfKit.line(doc, "Estado de despacho", String.valueOf(f.get("estadoDespachoNombre")));
            PdfKit.line(doc, "Generado", PdfKit.now());
        });
    }

    private void cabeceraAtencion(Document doc, Map<String, Object> a) throws Exception {
        PdfKit.line(doc, "Fecha de la cita", PdfKit.nz(a.get("citaInicio")));
        PdfKit.line(doc, "Propietario", a.get("propietario") + "  (Doc. " + PdfKit.nz(a.get("propietarioDocumento")) + ")");
        PdfKit.line(doc, "Mascota", a.get("nombreMascota") + " – " + a.get("especie") + " / " + a.get("raza"));
        PdfKit.line(doc, "Veterinario", a.get("medicoNombre") + " (" + a.get("especialidad") + ")");
        if (a.get("diagnostico") != null) {
            PdfKit.line(doc, "Diagnóstico", String.valueOf(a.get("diagnostico")));
        }
    }

    // ------------------------------------------------------------------ utilidades

    private Map<String, Object> cargar(long id) {
        Map<String, Object> a = db.must("La atención no existe.", SELECT_ATENCION + " WHERE a.id = ?", id);
        verificarMedico(In.toLong(a.get("medicoId")));
        return a;
    }

    private Map<String, Object> cargarEditable(long id) {
        db.must("La atención no existe.", "SELECT id FROM atencion_medica WHERE id = ? FOR UPDATE", id);
        Map<String, Object> a = cargar(id);
        if (!"EN_CURSO".equals(a.get("estado"))) {
            throw new BadRequestException("La atención ya fue finalizada y no puede modificarse.");
        }
        return a;
    }

    /** Un MEDICO solo accede a sus propias atenciones; el ADMINISTRADOR puede consultar. */
    private void verificarMedico(Long medicoId) {
        if (current.get().is("MEDICO")) {
            Long propio = current.medicoId();
            if (propio == null || !propio.equals(medicoId)) {
                throw new BadRequestException("Solo puede acceder a las atenciones de sus propios pacientes.",
                        org.springframework.http.HttpStatus.FORBIDDEN);
            }
        }
    }
}
