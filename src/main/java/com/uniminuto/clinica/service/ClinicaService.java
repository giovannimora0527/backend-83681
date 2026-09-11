package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.UsuarioRS;
import org.apache.coyote.BadRequestException;

public interface ClinicaService {

    String testService() throws BadRequestException;

    UsuarioRS testService2() throws BadRequestException;
}
