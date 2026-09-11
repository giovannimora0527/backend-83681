package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/usuario") // Si ya tienes el context-path en properties, puedes dejar solo "/usuario"
public interface UsuarioApi {

    @PostMapping("/crear")
    ResponseEntity<MiRespuestaRS> crear(@RequestBody Usuario usuario);

    @GetMapping("/listar")
    ResponseEntity<MiRespuestaRS> listar();

    @PostMapping("/actualizar") // Usan POST para actualizar según tu imagen
    ResponseEntity<MiRespuestaRS> actualizar(@RequestBody Usuario usuario);
}