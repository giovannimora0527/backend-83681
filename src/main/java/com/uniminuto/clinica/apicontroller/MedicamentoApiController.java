package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.entity.LogCargueMasivo;
import com.uniminuto.clinica.models.CargaMasivaPreviewRs;
import com.uniminuto.clinica.models.ConfirmarCargaMasivaRq;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CargaMasivaService;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MedicamentoApiController implements MedicamentoApi {

    @Autowired
    private MedicamentoService medicamentoService;

    @Autowired
    private CargaMasivaService cargaMasivaService;

    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.listarMedicamentos());
    }

    @Override
    public ResponseEntity<List<Medicamento>> consultarDisponiblesEnInventario() throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.consultarDisponiblesEnInventario());
    }

    @Override
    public ResponseEntity<Map<String, Object>> consultarEstadoInventario() throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.consultarEstadoInventario());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.registrarMedicamento(medicamentoRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.editarMedicamento(medicamentoRq));
    }

    @Override
    public ResponseEntity<CargaMasivaPreviewRs> previsualizarCargaMasiva(MultipartFile archivo) throws BadRequestException {
        return ResponseEntity.ok(this.cargaMasivaService.previsualizarArchivo(archivo));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> confirmarCargaMasiva(ConfirmarCargaMasivaRq solicitud) throws BadRequestException {
        return ResponseEntity.ok(this.cargaMasivaService.confirmarImportacion(solicitud));
    }

    @Override
    public ResponseEntity<List<LogCargueMasivo>> listarHistorialCargaMasiva() throws BadRequestException {
        return ResponseEntity.ok(this.cargaMasivaService.listarHistorialCargues());
    }
}
