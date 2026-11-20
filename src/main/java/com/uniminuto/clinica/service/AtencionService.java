package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.models.AtencionRq;
public interface AtencionService {
    AtencionMedica finalizar(AtencionRq request, Long usuarioId, String role);
    Object contexto(Integer mascotaId, Long usuarioId, String role);
    byte[] pdf(Long atencionId, Long usuarioId, String role);
}
