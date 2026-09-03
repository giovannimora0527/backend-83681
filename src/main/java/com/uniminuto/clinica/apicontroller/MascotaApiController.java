package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MascotaApi;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MascotaApiController implements MascotaApi {

    @Autowired
    private MascotaService mascotaService;

    @Override
    public ResponseEntity<List<Mascota>> getMascotas() throws BadRequestException {
        return ResponseEntity.ok(mascotaService.getListarMascotas());
    }

    @Override
    public ResponseEntity<Mascota> buscarMascotasPorNombre(String nombre) throws BadRequestException {
        return ResponseEntity.ok(mascotaService.buscarMascotaPorNombre(nombre));
    }
}