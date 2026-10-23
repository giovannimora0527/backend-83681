package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.EstadoHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;

import java.time.LocalDateTime;

/**
 * Fábrica de entidades de prueba reutilizada por las pruebas unitarias de los servicios.
 */
final class DatosPrueba {

    /**
     * Constructor privado: clase de utilidades estáticas.
     */
    private DatosPrueba() {
    }

    /**
     * Crea un cliente activo.
     *
     * @param id identificador del cliente.
     * @return cliente de prueba.
     */
    static Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setActivo(true);
        return cliente;
    }

    /**
     * Crea una mascota que pertenece al cliente indicado.
     *
     * @param id      identificador de la mascota.
     * @param cliente dueño de la mascota.
     * @return mascota de prueba.
     */
    static Mascota mascota(Long id, Cliente cliente) {
        Mascota mascota = new Mascota();
        mascota.setMascotaId(id);
        mascota.setNombreMascota("Max");
        mascota.setCliente(cliente);
        return mascota;
    }

    /**
     * Crea un veterinario.
     *
     * @param id identificador del veterinario.
     * @return veterinario de prueba.
     */
    static Medico medico(Long id) {
        Medico medico = new Medico();
        medico.setId(id);
        medico.setNombres("Laura");
        medico.setApellidos("Gómez");
        return medico;
    }

    /**
     * Crea una cita.
     *
     * @param id      identificador de la cita.
     * @param mascota mascota de la cita.
     * @param medico  veterinario de la cita.
     * @param fecha   fecha y hora de la cita.
     * @param estado  estado de la cita.
     * @return cita de prueba.
     */
    static Cita cita(Long id, Mascota mascota, Medico medico, LocalDateTime fecha, EstadoCita estado) {
        Cita cita = new Cita();
        cita.setCitaId(id);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaCita(fecha);
        cita.setEstado(estado);
        cita.setMotivo("Control");
        return cita;
    }

    /**
     * Crea una historia clínica abierta asociada a una cita.
     *
     * @param id   identificador de la historia.
     * @param cita cita que originó la historia.
     * @return historia de prueba.
     */
    static HistoriaMedica historia(Long id, Cita cita) {
        HistoriaMedica historia = new HistoriaMedica();
        historia.setHistoriaId(id);
        historia.setCita(cita);
        historia.setMascota(cita.getMascota());
        historia.setMedico(cita.getMedico());
        historia.setFechaConsulta(cita.getFechaCita());
        historia.setEstado(EstadoHistoria.ABIERTA);
        return historia;
    }
}
