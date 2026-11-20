package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.TarifaServicio;
import com.uniminuto.clinica.models.TarifaServicioRq;
import java.util.List;
public interface TarifaServicioService {
    List<TarifaServicio> listar();
    List<TarifaServicio> historial(String nombre);
    TarifaServicio crear(TarifaServicioRq request);
    TarifaServicio actualizar(Long id, TarifaServicioRq request);
}
