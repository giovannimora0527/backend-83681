package com.uniminuto.clinica.api;
import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.models.AtencionRq;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RequestMapping("/atenciones")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface AtencionApi {
    @PostMapping(value="/finalizar", consumes="application/json", produces="application/json")
    ResponseEntity<AtencionMedica> finalizar(@RequestBody AtencionRq request, HttpServletRequest httpRequest);
    @GetMapping("/contexto/{mascotaId}") ResponseEntity<?> contexto(@PathVariable Integer mascotaId, HttpServletRequest httpRequest);
    @GetMapping(value="/{atencionId}/pdf", produces="application/pdf") ResponseEntity<byte[]> pdf(@PathVariable Long atencionId, HttpServletRequest httpRequest);
}
