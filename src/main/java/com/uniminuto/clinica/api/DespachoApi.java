package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DespachoCancelarRq;
import com.uniminuto.clinica.models.DespachoHistorialEstadoRs;
import com.uniminuto.clinica.models.DespachoRq;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** API del módulo de consulta, preparación, confirmación y trazabilidad de despachos. */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/inventario/dispensaciones")
public interface DespachoApi {

    /** Lista los despachos registrados para el usuario de despacho autenticado. */
    @GetMapping(value = "/listar", produces = "application/json")
    ResponseEntity<List<DespachoRs>> listarDespachos(
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Consulta un despacho por su identificador. */
    @GetMapping(value = "/buscar/{despachoId}", produces = "application/json")
    ResponseEntity<DespachoRs> consultarDespacho(
            @PathVariable("despachoId") Long despachoId,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Consulta los despachos asociados a una fórmula. */
    @GetMapping(value = "/formula/{formulaId}", produces = "application/json")
    ResponseEntity<List<DespachoRs>> listarDespachosPorFormula(
            @PathVariable("formulaId") Long formulaId,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Consulta los productos, lotes, cantidades y movimientos de un despacho. */
    @GetMapping(value = "/{despachoId}/detalles", produces = "application/json")
    ResponseEntity<List<DetalleTrazabilidadRs>> listarDetalles(
            @PathVariable("despachoId") Long despachoId,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Consulta el historial de cambios de estado de un despacho. */
    @GetMapping(value = "/{despachoId}/historial-estados", produces = "application/json")
    ResponseEntity<List<DespachoHistorialEstadoRs>> listarHistorial(
            @PathVariable("despachoId") Long despachoId,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Busca fórmulas vigentes que aún tienen cantidades pendientes por entregar. */
    @GetMapping(value = "/formulas-pendientes", produces = "application/json")
    ResponseEntity<List<FormulaPendienteRs>> listarFormulasPendientes(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaGeneracion,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Consulta una fórmula con disponibilidad por lote para preparar su despacho. */
    @GetMapping(value = "/formula/{formulaId}/preparacion", produces = "application/json")
    ResponseEntity<FormulaPendienteRs> consultarPreparacionFormula(
            @PathVariable("formulaId") Long formulaId,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Consulta el historial de despachos y sus detalles. */
    @GetMapping(value = "/historial", produces = "application/json")
    ResponseEntity<List<DespachoHistorialRs>> consultarHistorialDespachos(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Consulta las entregas y reversiones realizadas para un medicamento. */
    @GetMapping(value = "/medicamento/{medicamentoId}/historial", produces = "application/json")
    ResponseEntity<List<DetalleTrazabilidadRs>> consultarHistorialMedicamento(
            @PathVariable("medicamentoId") Long medicamentoId,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Registra el inicio de preparación sin afectar existencias ni movimientos. */
    @PostMapping(value = {"/preparar", "/guardar"}, produces = "application/json", consumes = "application/json")
    ResponseEntity<DespachoRs> prepararDespacho(
            @RequestBody DespachoRq despachoRq,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Confirma las cantidades efectivamente entregadas de los lotes seleccionados. */
    @PostMapping(value = "/{despachoId}/confirmar", produces = "application/json", consumes = "application/json")
    ResponseEntity<DespachoRs> confirmarDespacho(
            @PathVariable("despachoId") Long despachoId,
            @RequestBody ConfirmacionDespachoRq confirmacionRq,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Cancela mediante reversión trazable las cantidades ya entregadas. */
    @PostMapping(value = "/{despachoId}/cancelar", produces = "application/json", consumes = "application/json")
    ResponseEntity<MiRespuestaRS> cancelarDespacho(
            @PathVariable("despachoId") Long despachoId,
            @RequestBody DespachoCancelarRq cancelacionRq,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    @PostMapping(value = "/{despachoId}/cerrar-pendientes", produces = "application/json", consumes = "application/json")
    ResponseEntity<MiRespuestaRS> cerrarPendientes(
            @PathVariable("despachoId") Long despachoId,
            @RequestBody DespachoCancelarRq solicitud,
            @AuthenticationPrincipal String usuarioResponsableId);

    /** Genera y descarga el comprobante PDF asociado al despacho. */
    @GetMapping(value = "/{despachoId}/comprobante", produces = "application/pdf")
    ResponseEntity<byte[]> descargarComprobante(
            @PathVariable("despachoId") Long despachoId,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Datos de una fórmula pendiente y su disponibilidad actual para el despacho. */
    @Data
    class FormulaPendienteRs {
        private Long formulaId;
        private String numeroFormula;
        private LocalDateTime fechaGeneracion;
        private Long medicoId;
        private String medicoNombre;
        private Long propietarioId;
        private String propietarioNombre;
        private Long mascotaId;
        private String mascotaNombre;
        private String indicaciones;
        private String estadoDespacho;
        private List<MedicamentoFormuladoRs> medicamentos;
    }

    /** Detalle prescrito junto con la cantidad entregada y los lotes elegibles. */
    @Data
    class MedicamentoFormuladoRs {
        private Long formulaDetalleId;
        private Long medicamentoId;
        private String medicamentoNombre;
        private String dosis;
        private String frecuencia;
        private String duracionTratamiento;
        private String viaAdministracion;
        private String indicaciones;
        private Integer cantidadPrescrita;
        private Integer cantidadDispensada;
        private Integer cantidadPendiente;
        private Integer cantidadDisponible;
        private Integer cantidadFaltante;
        private boolean disponibilidadSuficiente;
        private List<LoteDisponibleRs> lotes;
    }

    /** Lote visible en la preparación, incluso si está vencido y no es seleccionable. */
    @Data
    class LoteDisponibleRs {
        private Long loteId;
        private String numeroLote;
        private Integer cantidadDisponible;
        private LocalDate fechaVencimiento;
        private boolean vencido;
        private boolean proximoAVencer;
        private boolean recomendadoFefo;
        private boolean seleccionable;
    }

    /** Solicitud para confirmar una dispensación y protegerla contra reintentos duplicados. */
    @Data
    class ConfirmacionDespachoRq {
        private String claveIdempotencia;
        private String observacion;
        private List<LineaConfirmacionRq> detalles;
    }

    /** Cantidad efectivamente entregada de un lote para una línea prescrita. */
    @Data
    class LineaConfirmacionRq {
        private Long formulaDetalleId;
        private Long loteId;
        private Integer cantidadEntregada;
    }

    /** Información de trazabilidad de una línea despachada. */
    @Data
    class DetalleTrazabilidadRs {
        private Long id;
        private Long despachoId;
        private String numeroDespacho;
        private Long formulaId;
        private String numeroFormula;
        private Long formulaDetalleId;
        private Long medicamentoId;
        private String medicamentoNombre;
        private Long loteId;
        private String numeroLote;
        private LocalDate fechaVencimiento;
        private Integer cantidadFormulada;
        private Integer cantidadEntregada;
        private Long movimientoInventarioId;
        private Long movimientoReversionId;
        private LocalDateTime movimientoFecha;
        private Long movimientoUsuarioId;
        private String movimientoUsuarioNombre;
        private LocalDateTime movimientoReversionFecha;
        private Long movimientoReversionUsuarioId;
        private String movimientoReversionUsuarioNombre;
        private LocalDateTime fechaDespacho;
        private String estadoDespacho;
        private Long usuarioDespachoId;
        private String usuarioDespachoNombre;
    }

    /** Registro histórico de un despacho con responsable y sus líneas entregadas. */
    @Data
    class DespachoHistorialRs {
        private Long id;
        private String numeroDespacho;
        private String numeroComprobante;
        private String urlComprobantePdf;
        private Long formulaId;
        private String numeroFormula;
        private LocalDateTime fechaGeneracionFormula;
        private Long citaId;
        private Long clienteId;
        private String propietarioNombre;
        private Long mascotaId;
        private String mascotaNombre;
        private String medicoNombre;
        private Long usuarioDespachoId;
        private String usuarioDespachoNombre;
        private LocalDateTime fechaDespacho;
        private String estado;
        private String observacion;
        private List<DetalleTrazabilidadRs> detalles;
    }
}
