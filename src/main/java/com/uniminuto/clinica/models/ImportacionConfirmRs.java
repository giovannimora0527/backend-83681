package com.uniminuto.clinica.models;
import java.util.List;
public record ImportacionConfirmRs(Long importacionId, int creadas, int fallidas, List<ImportacionFila> filas) { }
