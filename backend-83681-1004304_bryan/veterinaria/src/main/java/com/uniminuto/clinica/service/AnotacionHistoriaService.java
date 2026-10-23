package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.stereotype.Service;

@Service
public interface AnotacionHistoriaService {

    MiRespuestaRS crearAnotacionHistoria(AnotacionRq anotacionRq) throws BadRequestException;
}
