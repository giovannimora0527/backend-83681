package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.DespachoHistorialEstado;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DespachoCancelarRq;
import com.uniminuto.clinica.models.DespachoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDate;
import java.util.List;

/** Casos de uso para la consulta, preparación y dispensación de fórmulas médicas. */
public interface DespachoService {

    /** Lista los despachos registrados para el personal autorizado. */
    List<Despacho> listarDespachos(String usuarioResponsableId);

    /** Consulta un despacho validando el rol del usuario. */
    Despacho consultarDespacho(Long despachoId, String usuarioResponsableId) throws BadRequestException;

    /** Busca los despachos relacionados con una fórmula. */
    List<Despacho> listarDespachosPorFormula(Long formulaId, String usuarioResponsableId)
            throws BadRequestException;

    /** Lista las líneas y movimientos de inventario de un despacho. */
    List<DespachoDetalle> listarDetalles(Long despachoId, String usuarioResponsableId)
            throws BadRequestException;

    /** Lista los cambios de estado de un despacho. */
    List<DespachoHistorialEstado> listarHistorial(Long despachoId, String usuarioResponsableId)
            throws BadRequestException;

    /** Busca fórmulas con cantidades pendientes y enriquece cada línea con existencias y lotes. */
    List<FormulaMedica> listarFormulasPendientes(String filtro, LocalDate fechaGeneracion,
                                                String usuarioResponsableId) throws BadRequestException;

    /** Consulta una fórmula específica con los datos necesarios para preparar su despacho. */
    FormulaMedica consultarFormulaParaDespacho(Long formulaId, String usuarioResponsableId)
            throws BadRequestException;

    /** Crea o recupera una preparación sin descontar existencias. */
    Despacho prepararDespacho(DespachoRq despachoRq, String usuarioResponsableId)
            throws BadRequestException;

    /** Confirma en una sola transacción las cantidades entregadas y sus movimientos de salida. */
    Despacho confirmarDespacho(Long despachoId, List<LineaConfirmacion> detalles,
                               String claveIdempotencia, String observacion,
                               String usuarioResponsableId) throws BadRequestException;

    /** Cancela el despacho y revierte trazablemente cualquier salida ya confirmada. */
    MiRespuestaRS cancelarDespacho(Long despachoId, DespachoCancelarRq cancelacionRq,
                                   String usuarioResponsableId) throws BadRequestException;

    MiRespuestaRS cerrarPendientes(Long despachoId, DespachoCancelarRq solicitud, String usuarioResponsableId);

    /** Consulta el historial de despachos con filtros opcionales de texto y fechas. */
    List<Despacho> consultarHistorialDespachos(String filtro, LocalDate fechaDesde, LocalDate fechaHasta,
                                               String usuarioResponsableId) throws BadRequestException;

    /** Consulta las entregas asociadas a un medicamento. */
    List<DespachoDetalle> consultarHistorialMedicamento(Long medicamentoId, String usuarioResponsableId)
            throws BadRequestException;

    /** Construye el comprobante PDF del despacho confirmado. */
    byte[] generarComprobantePdf(Long despachoId, String usuarioResponsableId) throws BadRequestException;

    /** Línea de confirmación recibida desde la interfaz de despacho. */
    class LineaConfirmacion {
        private final Long formulaDetalleId;
        private final Long loteId;
        private final Integer cantidadEntregada;

        /** Crea la selección de lote y cantidad efectiva de una línea prescrita. */
        public LineaConfirmacion(Long formulaDetalleId, Long loteId, Integer cantidadEntregada) {
            this.formulaDetalleId = formulaDetalleId;
            this.loteId = loteId;
            this.cantidadEntregada = cantidadEntregada;
        }

        /** Devuelve el identificador de la línea de la fórmula. */
        public Long getFormulaDetalleId() {
            return formulaDetalleId;
        }

        /** Devuelve el lote seleccionado para la entrega. */
        public Long getLoteId() {
            return loteId;
        }

        /** Devuelve la cantidad que se entregará efectivamente. */
        public Integer getCantidadEntregada() {
            return cantidadEntregada;
        }
    }
}
