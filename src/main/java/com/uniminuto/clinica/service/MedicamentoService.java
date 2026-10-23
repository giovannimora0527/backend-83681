package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface MedicamentoService {

    List<Medicamento> listarMedicamentos();

    MiRespuestaRS crearMedicamento(MedicamentoRq rq);

    MiRespuestaRS actualizarMedicamento(MedicamentoRq rq);

    MiRespuestaRS eliminarMedicamento(Long id);
}
