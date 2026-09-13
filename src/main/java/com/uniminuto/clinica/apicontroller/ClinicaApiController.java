package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClinicaApi;
import com.uniminuto.clinica.service.ClinicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.RestController;
import com.uniminuto.clinica.models.MiRespuestaRs;

@RestController
public class ClinicaApiController implements ClinicaApi {

    @Autowired
    private ClinicaService clinicaService;

    @Override
    public ResponseEntity<String> testService() throws BadRequestException {
        return ResponseEntity.ok(this.clinicaService.testService());
    }

    @Override
    public ResponseEntity<MiRespuestaRs> testService2() throws BadRequestException {
        return ResponseEntity.ok(this.clinicaService.testService2());
    }


}
