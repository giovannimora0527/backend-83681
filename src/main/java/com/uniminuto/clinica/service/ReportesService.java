package com.uniminuto.clinica.service;
import java.time.LocalDateTime;
import java.util.Map;
public interface ReportesService {
    Map<String,Object> inventario();
    Map<String,Object> citas(LocalDateTime desde, LocalDateTime hasta, Long medicoId);
    Map<String,Object> financiero(LocalDateTime desde, LocalDateTime hasta);
    byte[] excel(String tipo, LocalDateTime desde, LocalDateTime hasta, Long medicoId);
    byte[] pdf(String tipo, LocalDateTime desde, LocalDateTime hasta, Long medicoId);
}
