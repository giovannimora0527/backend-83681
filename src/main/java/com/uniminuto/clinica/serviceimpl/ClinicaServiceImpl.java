package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClinicaService;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class ClinicaServiceImpl implements ClinicaService {


    @Override
    public String testService() throws BadRequestException {
        return "Servicio funcionando correctamente";
    }

    @Override
    public MiRespuestaRS testService2() throws BadRequestException {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Servicio funcionando correctamente desde un objeto json personalizado");
        return respuesta;
    }
}
