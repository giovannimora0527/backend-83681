package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;
import java.time.LocalDate;
import com.uniminuto.clinica.models.ProductoRq;

public interface MedicamentoService {

    List<Medicamento> listarMedicamentos() throws BadRequestException;
    Medicamento consultar(Long id);
    Medicamento crear(ProductoRq producto);
    Medicamento actualizar(Long id, ProductoRq producto);
    List<Medicamento> listarStockBajo();
    List<Medicamento> listarAgotados();
    List<Medicamento> listarPorVencer(LocalDate hasta);
    List<Medicamento> listarVencidos();
}
