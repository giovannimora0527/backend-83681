package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CatalogoService;
import com.uniminuto.clinica.util.Validaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de las consultas de catálogo.
 */
@Service
public class CatalogoServiceImpl implements CatalogoService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    /** {@inheritDoc} */
    @Override
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public Cliente consultarCliente(Long id) {
        Validaciones.idValido(id, "cliente");
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El cliente solicitado no existe"));
    }

    /** {@inheritDoc} */
    @Override
    public List<Mascota> listarMascotasDeCliente(Long clienteId) {
        Validaciones.idValido(clienteId, "cliente");
        if (!clienteRepository.existsById(clienteId)) {
            throw new NotFoundException("El cliente solicitado no existe");
        }
        return mascotaRepository.findAllByClienteIdOrderByNombreMascotaAsc(clienteId);
    }

    /** {@inheritDoc} */
    @Override
    public Mascota consultarMascota(Integer id) {
        Validaciones.idValido(id, "mascota");
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La mascota solicitada no existe"));
    }

    /** {@inheritDoc} */
    @Override
    public List<Medico> listarVeterinarios() {
        return medicoRepository.findAll();
    }
}
