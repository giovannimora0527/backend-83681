
package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    @GetMapping(
            value = "/listar-ordenado",
            produces = {"application/json"}
    )
    ResponseEntity<List<FormulaMedica>> getFormulasByOrden(
            @RequestParam boolean asc
    ) throws BadRequestException;

    @PostMapping(
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<FormulaMedica> crearFormula(
            @RequestBody FormulaMedica formula
    );

    @GetMapping(value = "/{id}", produces = {"application/json"})
    ResponseEntity<FormulaMedica> consultarFormula(@PathVariable Long id);

    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<FormulaMedica> actualizarFormula(@PathVariable Long id, @RequestBody FormulaMedica formula);
}
