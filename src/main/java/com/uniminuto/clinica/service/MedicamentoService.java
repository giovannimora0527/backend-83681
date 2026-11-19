package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.InformeInventarioRs;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

/** Define los casos de uso de administración y consulta de medicamentos. */
public interface MedicamentoService {

    /** Consulta los medicamentos. */
    List<Medicamento> listarMedicamentos() throws BadRequestException;

    /** Busca los medicamentos. */
    List<Medicamento> buscarMedicamentos(String termino);

    /** Lista productos con existencias positivas. */
    List<Medicamento> listarDisponibles();

    /** Lista productos agotados. */
    List<Medicamento> listarAgotados();

    /** Lista productos con existencias inferiores o iguales al stock mínimo. */
    List<Medicamento> listarStockBajo();

    /** Lista productos que vencen dentro del número de días indicado. */
    List<Medicamento> listarProximosAVencer(int dias);

    /** Consulta la existencia utilizable y si alcanza para una cantidad requerida. */
    Map<String, Object> consultarDisponibilidad(Long medicamentoId, Integer cantidadRequerida);

    /** Genera el informe de inventario en formato Excel con filtros opcionales. */
    byte[] exportarInventarioExcel(String tipo, LocalDate desde, LocalDate hasta);

    /** Consulta el informe de inventario. */
    InformeInventarioRs consultarInformeInventario(String tipo, LocalDate desde, LocalDate hasta);

    /** Registra el medicamento. */
    MiRespuestaRS crearMedicamento(MedicamentoRq medicamentoRq, String usuarioResponsableId);

    /** Actualiza el medicamento. */
    MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq, String usuarioResponsableId);
}
