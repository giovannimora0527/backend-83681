package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.IngresoLoteRq;
import com.uniminuto.clinica.models.InventarioItemRs;
import com.uniminuto.clinica.models.LoteInventarioRs;
import com.uniminuto.clinica.models.MovimientoInventarioRs;
import com.uniminuto.clinica.models.DespachoPendienteRs;
import com.uniminuto.clinica.models.ComprobanteDespachoRs;

import java.util.List;

public interface InventarioService {

    List<InventarioItemRs> obtenerResumen(String estado);

    List<LoteInventarioRs> obtenerLotes(Long medicamentoId) throws BadRequestException;

    List<MovimientoInventarioRs> obtenerMovimientos(Long medicamentoId) throws BadRequestException;

    LoteInventarioRs registrarIngreso(IngresoLoteRq request) throws BadRequestException;

    List<DespachoPendienteRs> obtenerDespachosPendientes();

    ComprobanteDespachoRs obtenerComprobanteDespacho(Long formulaId);
}
