package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.models.ConfirmarDespachoRq;
import com.uniminuto.clinica.entity.FormulaDetalle;
import com.uniminuto.clinica.entity.DespachoDetalle;
import java.util.List;
public interface DespachoService {
    Despacho confirmar(ConfirmarDespachoRq request, Long usuarioId);
    List<FormulaDetalle> pendientes();
    List<java.util.Map<String,Object>> buscarPendientes(Long formulaId,Integer mascotaId,String propietario,java.time.LocalDateTime desde,java.time.LocalDateTime hasta);
    List<java.util.Map<String,Object>> historial(java.time.LocalDateTime desde,java.time.LocalDateTime hasta,Integer mascotaId,String propietario);
    List<DespachoDetalle> detalles(Long despachoId);
    Despacho revertir(Long despachoId, String motivo, Long usuarioId);
}
