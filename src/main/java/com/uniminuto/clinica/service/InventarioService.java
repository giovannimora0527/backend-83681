package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.InventarioMovimiento;
import com.uniminuto.clinica.models.EntradaLoteRq;
import java.time.LocalDate;
import java.util.List;

public interface InventarioService {
    InventarioLote registrarEntrada(EntradaLoteRq entrada);
    List<InventarioLote> consultarLotes(Long medicamentoId);
    List<InventarioLote> consultarPorVencimiento(LocalDate hasta);
    List<InventarioMovimiento> movimientos(Long medicamentoId);
}
