package com.uniminuto.clinica.models;
import java.util.List;
public record ImportacionPreviewRs(Long importacionId, int totalFilas, int validas, int invalidas, List<ImportacionFila> filas) { }
