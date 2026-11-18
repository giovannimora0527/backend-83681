package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CargaMasivaPreviewRs;
import com.uniminuto.clinica.models.ConfirmarCargaMasivaRq;
import com.uniminuto.clinica.entity.LogCargueMasivo;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medicamento")
public interface MedicamentoApi {

    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos()
            throws BadRequestException;

    @GetMapping(value = "/disponibles",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medicamento>> consultarDisponiblesEnInventario()
            throws BadRequestException;

    @GetMapping(value = "/estado",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Map<String, Object>> consultarEstadoInventario()
            throws BadRequestException;

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMedicamento(@RequestBody MedicamentoRq medicamentoRq)
            throws BadRequestException;

    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedicamento(@RequestBody MedicamentoRq medicamentoRq)
            throws BadRequestException;

    @PostMapping(value = "/carga-masiva/previsualizar",
            consumes = {"multipart/form-data"},
            produces = {"application/json"})
    ResponseEntity<CargaMasivaPreviewRs> previsualizarCargaMasiva(@RequestParam("archivo") MultipartFile archivo)
            throws BadRequestException;

    @PostMapping(value = "/carga-masiva/confirmar",
            consumes = {"application/json"},
            produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> confirmarCargaMasiva(@RequestBody ConfirmarCargaMasivaRq solicitud)
            throws BadRequestException;

    @GetMapping(value = "/carga-masiva/historial",
            produces = {"application/json"})
    ResponseEntity<List<LogCargueMasivo>> listarHistorialCargaMasiva()
            throws BadRequestException;
}
