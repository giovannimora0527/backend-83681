package com.uniminuto.clinica.service;

import java.util.Map;

/** Ejecuta la persistencia transaccional aislada de un registro importado. */
public interface CargaMasivaRegistroService {

    /** Persiste un producto y su lote inicial dentro de una transacción independiente. */
    void guardarRegistro(Map<String, String> registro, Long usuarioId);
}
