package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.LogCargueMasivo;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CargaMasivaPreviewRs;
import com.uniminuto.clinica.models.ConfirmarCargaMasivaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CargaMasivaService {

    CargaMasivaPreviewRs previsualizarArchivo(MultipartFile archivo) throws BadRequestException;

    MiRespuestaRS confirmarImportacion(ConfirmarCargaMasivaRq solicitud) throws BadRequestException;

    List<LogCargueMasivo> listarHistorialCargues();
}
