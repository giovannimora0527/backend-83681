package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MascotaServiceImpl implements MascotaService {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private RazaRepository razaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public List<Mascota> getListarMascotas() {
        return mascotaRepository.findAllByOrderByNombreMascotaAsc();
    }

    @Override
    public Mascota buscarMascotaPorNombre(String nombre) throws BadRequestException {
       if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre de la mascota no puede estar vacío");
        }
        Mascota mascota = mascotaRepository.findByNombreMascota(nombre);
        if (mascota == null) {
            throw new BadRequestException("No se encontró la mascota con el nombre: " + nombre);
        }
        return mascota;
    }

    @Override
    public List<Mascota> getListarMascotasOrdenadas(boolean ascendente) {
        if (ascendente) {
            return mascotaRepository.findAllByOrderByNombreMascotaAsc();
        } else {
            return mascotaRepository.findAllByOrderByNombreMascotaDesc();
        }
    }

    @Override
    public UsuarioRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(mascotaRq);

        // Paso 2. Validar si la raza existe en la base de datos
        Optional<Raza> optRaza = this.razaRepository.findById(mascotaRq.getRazaId());
        if (optRaza.isEmpty()) {
            throw new BadRequestException("La raza con ID " + mascotaRq.getRazaId() + " no existe en la base de datos");
        }

        // Paso 3. Validar si el cliente existe en la base de datos
        Optional<Cliente> optCliente = this.clienteRepository.findById(mascotaRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + mascotaRq.getClienteId() + " no existe en la base de datos");
        }

        // Crear la entidad mascota a guardar.
        Mascota mascota = new Mascota();
        mascota.setNombreMascota(mascotaRq.getNombreMascota());
        mascota.setEdad(mascotaRq.getEdad());
        mascota.setRaza(optRaza.get());
        mascota.setCliente(optCliente.get());
        mascota.setFechaRegistro(LocalDateTime.now());

        this.mascotaRepository.save(mascota);

        // Paso 4. Retornar la respuesta
        UsuarioRS respuesta = new UsuarioRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Mascota guardada correctamente");

        return respuesta;
    }

    @Override
    public UsuarioRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(mascotaRq);

        // Paso 2. Validar si la raza existe en la base de datos
        Optional<Raza> optRaza = this.razaRepository.findById(mascotaRq.getRazaId());
        if (optRaza.isEmpty()) {
            throw new BadRequestException("La raza con ID " + mascotaRq.getRazaId() + " no existe en la base de datos");
        }

        // Paso 3. Validar si el cliente existe en la base de datos
        Optional<Cliente> optCliente = this.clienteRepository.findById(mascotaRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + mascotaRq.getClienteId() + " no existe en la base de datos");
        }

        Optional<Mascota> optMascota = this.mascotaRepository.findById(mascotaRq.getMascotaId());
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + mascotaRq.getMascotaId() + " no existe en la base de datos");
        }

        Mascota mascota = optMascota.get();
        mascota.setNombreMascota(mascotaRq.getNombreMascota());
        mascota.setEdad(mascotaRq.getEdad());
        mascota.setRaza(optRaza.get());
        mascota.setCliente(optCliente.get());
        mascota.setFechaModificacion(LocalDateTime.now());

        this.mascotaRepository.save(mascota);

        // Paso 4. Retornar la respuesta
        UsuarioRS respuesta = new UsuarioRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Mascota actualizada correctamente");

        return respuesta;
    }

    private void validarObjetoEntrada(MascotaRq mascotaRq) throws BadRequestException {
         if (mascotaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
         }

         if (mascotaRq.getNombreMascota() == null || mascotaRq.getNombreMascota().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la mascota no puede estar vacío");
         }

        if (mascotaRq.getEdad() == null || mascotaRq.getEdad() < 0) {
            throw new BadRequestException("La edad de la mascota no puede estar vacía ni tener valores negativos");
        }

        if (mascotaRq.getRazaId() == null || mascotaRq.getRazaId() < 0) {
            throw new BadRequestException("El ID de la raza de la mascota no puede estar vacío ni tener valores negativos");
        }

        if (mascotaRq.getClienteId() == null || mascotaRq.getClienteId() < 0) {
            throw new BadRequestException("El ID del cliente de la mascota no puede estar vacío ni tener valores negativos");
        }
    }
}
