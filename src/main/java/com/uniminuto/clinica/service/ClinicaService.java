package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.MiRespuestaRs;
import org.apache.coyote.BadRequestException;

public interface ClinicaService {

    String testService() throws BadRequestException;

    MiRespuestaRs testService2() throws BadRequestException;
}
