package com.uniminuto.clinica.modulos.inventario;

import com.uniminuto.clinica.support.Http;
import com.uniminuto.clinica.support.In;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventario")
@Tag(name = "1. Inventario", description = "Medicamentos y productos, lotes, entradas, kárdex y carga masiva. "
        + "Lectura: ADMINISTRADOR, RECEPCION, MEDICO, DESPACHO_MEDICO. Escritura: solo ADMINISTRADOR.")
public class InventarioController {

    private final InventarioService service;
    private final CargaMasivaService carga;

    public InventarioController(InventarioService service, CargaMasivaService carga) {
        this.service = service;
        this.carga = carga;
    }

    @Operation(summary = "Lista medicamentos/productos con existencias, precios y banderas de estado",
            description = "Filtros: q (nombre, código o Invima), estado (AGOTADO | STOCK_BAJO | POR_VENCER | VENCIDO | DISPONIBLE), "
                    + "tipo (MEDICAMENTO | PRODUCTO), soloActivos.")
    @GetMapping("/medicamentos")
    public List<Map<String, Object>> listar(@RequestParam(required = false) String q,
                                            @RequestParam(required = false) String estado,
                                            @RequestParam(required = false) String tipo,
                                            @RequestParam(defaultValue = "false") boolean soloActivos) {
        return service.listar(q, estado, tipo, soloActivos);
    }

    @Operation(summary = "Contadores de alertas: agotados, stock bajo, por vencer y vencidos")
    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return service.resumen();
    }

    @Operation(summary = "Consulta un medicamento/producto")
    @GetMapping("/medicamentos/{id}")
    public Map<String, Object> obtener(@PathVariable long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Registra un medicamento/producto",
            description = "Campos: nombre, tipo, codigo (8 alfanuméricos, opcional: se genera), registroInvima (obligatorio en medicamentos), "
                    + "precioCompra, precioVenta, ivaPorcentaje, stockMinimo, fechaProximoPedido; opcionales cantidadInicial, numeroLote, fechaVencimiento.")
    @PostMapping("/medicamentos")
    public Map<String, Object> crear(@RequestBody Map<String, Object> body) {
        return service.crear(body);
    }

    @Operation(summary = "Edita un medicamento/producto (las existencias solo cambian con entradas, bajas o despachos)")
    @PutMapping("/medicamentos/{id}")
    public Map<String, Object> actualizar(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.actualizar(id, body);
    }

    @Operation(summary = "Activa o inactiva un medicamento (no se elimina para conservar el historial)")
    @PatchMapping("/medicamentos/{id}/estado")
    public Map<String, Object> estado(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.cambiarEstado(id, In.bool(body, "activo", true));
    }

    @Operation(summary = "Lotes del medicamento con fechas de vencimiento y cantidades")
    @GetMapping("/medicamentos/{id}/lotes")
    public List<Map<String, Object>> lotes(@PathVariable long id) {
        return service.lotes(id);
    }

    @Operation(summary = "Entrada de inventario (crea un lote y registra el movimiento ENTRADA)",
            description = "Campos: cantidad, numeroLote, fechaIngreso, fechaVencimiento, precioCompra.")
    @PostMapping("/medicamentos/{id}/entradas")
    public Map<String, Object> entrada(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.entrada(id, body);
    }

    @Operation(summary = "Historial (kárdex) de entradas y salidas del medicamento")
    @GetMapping("/medicamentos/{id}/movimientos")
    public List<Map<String, Object>> movimientos(@PathVariable long id,
                                                 @RequestParam(required = false) String inicio,
                                                 @RequestParam(required = false) String fin,
                                                 @RequestParam(required = false) String tipo) {
        return service.movimientos(id,
                inicio == null || inicio.isBlank() ? null : In.parseDate(inicio, "inicio"),
                fin == null || fin.isBlank() ? null : In.parseDate(fin, "fin"), tipo);
    }

    @Operation(summary = "Baja de unidades de un lote (vencimiento, daño). Genera movimiento AJUSTE")
    @PostMapping("/lotes/{loteId}/baja")
    public Map<String, Object> baja(@PathVariable long loteId, @RequestBody Map<String, Object> body) {
        return service.bajaLote(loteId, body);
    }

    // ------------------------------------------------------------------ carga masiva

    @Operation(summary = "Descarga la plantilla Excel para la carga masiva")
    @GetMapping("/carga/plantilla")
    public ResponseEntity<byte[]> plantilla() {
        return Http.xlsx(carga.plantilla(), "plantilla_inventario.xlsx");
    }

    @Operation(summary = "Paso 1 - Vista previa: valida estructura y filas de un .xlsx/.csv (multipart 'archivo')")
    @PostMapping(value = "/carga/previsualizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> previsualizar(@RequestParam("archivo") MultipartFile archivo) {
        return carga.previsualizar(archivo);
    }

    @Operation(summary = "Paso 2 - Confirma la importación de los registros válidos (cada fila en su propia transacción)")
    @PostMapping("/carga/{id}/confirmar")
    public Map<String, Object> confirmar(@PathVariable long id) {
        return carga.confirmar(id);
    }

    @Operation(summary = "Cancela una carga previsualizada sin importar nada")
    @PostMapping("/carga/{id}/cancelar")
    public Map<String, Object> cancelar(@PathVariable long id) {
        return carga.cancelar(id);
    }

    @Operation(summary = "Log de cargues: archivo, fecha, usuario y resultados")
    @GetMapping("/carga")
    public List<Map<String, Object>> log() {
        return carga.log();
    }

    @Operation(summary = "Detalle de una carga con todas sus filas (correctas y con error)")
    @GetMapping("/carga/{id}")
    public Map<String, Object> detalle(@PathVariable long id) {
        return carga.detalle(id);
    }

    @Operation(summary = "Informe de errores en Excel: fila y motivo del rechazo")
    @GetMapping("/carga/{id}/errores")
    public ResponseEntity<byte[]> errores(@PathVariable long id) {
        return Http.xlsx(carga.informeErrores(id), "errores_carga_" + id + ".xlsx");
    }
}
