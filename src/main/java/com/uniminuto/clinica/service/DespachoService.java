package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.DespachoHistorialEstado;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DespachoCancelarRq;
import com.uniminuto.clinica.models.DespachoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface DespachoService {

    List<Despacho> listarDespachos();

    Despacho consultarDespacho(Long despachoId) throws BadRequestException;

    List<Despacho> listarDespachosPorFormula(Long formulaId) throws BadRequestException;

    List<DespachoDetalle> listarDetalles(Long despachoId) throws BadRequestException;

    List<DespachoHistorialEstado> listarHistorial(Long despachoId) throws BadRequestException;

    Despacho guardarDespacho(DespachoRq despachoRq, String usuarioResponsableId)
            throws BadRequestException;

    MiRespuestaRS cancelarDespacho(Long despachoId, DespachoCancelarRq cancelacionRq,
                                   String usuarioResponsableId)
            throws BadRequestException;
}
