package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

public interface ClinicaService {

    String testService() throws BadRequestException;

    MiRespuestaRS testService2() throws BadRequestException;
}
