package com.uniminuto.clinica.util;

import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resuelve nombres a partir de ids en lote (una consulta por entidad).
 * Permite que los listados muestren nombres sin cargar relaciones JPA ni hacer consultas por fila.
 */
@Component
public class ResolutorNombres {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CitaRepository citaRepository;

    /**
     * @param ids ids de clientes (pueden repetirse o ser nulos).
     * @return mapa id -> nombre completo del cliente.
     */
    public Map<Long, String> clientes(Collection<Long> ids) {
        Map<Long, String> mapa = new HashMap<>();
        if (ids.isEmpty()) {
            return mapa;
        }
        clienteRepository.findAllById(limpiar(ids)).forEach(c ->
                mapa.put(c.getClienteId(), unir(c.getNombres(), c.getApellidos())));
        return mapa;
    }

    /**
     * @param ids ids de mascotas.
     * @return mapa id -> nombre de la mascota.
     */
    public Map<Integer, String> mascotas(Collection<Integer> ids) {
        Map<Integer, String> mapa = new HashMap<>();
        if (ids.isEmpty()) {
            return mapa;
        }
        mascotaRepository.findAllById(limpiar(ids)).forEach(m ->
                mapa.put(m.getMascotaId(), Objects.toString(m.getNombreMascota(), "")));
        return mapa;
    }

    /**
     * @param ids ids de veterinarios.
     * @return mapa id -> nombre completo del veterinario.
     */
    public Map<Long, String> medicos(Collection<Long> ids) {
        Map<Long, String> mapa = new HashMap<>();
        if (ids.isEmpty()) {
            return mapa;
        }
        medicoRepository.findAllById(limpiar(ids)).forEach(m ->
                mapa.put(m.getId(), unir(m.getNombres(), m.getApellidos())));
        return mapa;
    }

    /**
     * @param ids ids de citas.
     * @return mapa id -> estado de la cita.
     */
    public Map<Long, String> estadosCitas(Collection<Long> ids) {
        Map<Long, String> mapa = new HashMap<>();
        if (ids.isEmpty()) {
            return mapa;
        }
        citaRepository.findAllById(limpiar(ids)).forEach(c -> mapa.put(c.getId(), c.getEstado()));
        return mapa;
    }

    /**
     * Quita nulos y repetidos para que las consultas en lote sean válidas.
     *
     * @param ids colección original.
     * @return lista limpia.
     */
    private <T> List<T> limpiar(Collection<T> ids) {
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    /**
     * Une nombres y apellidos omitiendo los valores vacíos.
     *
     * @param nombres   nombres.
     * @param apellidos apellidos.
     * @return texto unido sin espacios sobrantes.
     */
    private String unir(String nombres, String apellidos) {
        return (Objects.toString(nombres, "") + " " + Objects.toString(apellidos, "")).trim();
    }
}
