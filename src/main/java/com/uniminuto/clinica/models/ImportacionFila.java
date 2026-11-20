package com.uniminuto.clinica.models;
import java.util.List;
public record ImportacionFila(int fila, ProductoRq producto, EntradaLoteRq lote, List<String> errores) {
    public boolean valida() { return errores == null || errores.isEmpty(); }
}
