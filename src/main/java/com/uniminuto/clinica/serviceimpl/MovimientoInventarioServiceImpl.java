package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.service.MovimientoInventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Locale;

/** Implementa las consultas históricas de entradas y salidas. */
@Service
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final MedicamentoRepository medicamentoRepository;

    /** Construye el servicio con los repositorios de movimientos y medicamentos. */
    public MovimientoInventarioServiceImpl(MovimientoInventarioRepository movimientoRepository,
                                           MedicamentoRepository medicamentoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarPorMedicamento(Long medicamentoId) {
        validarConsultaMovimientos();
        if (medicamentoId == null || medicamentoId <= 0) {
            throw new BadRequestException("El id del medicamento debe ser mayor a cero.");
        }
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new BadRequestException("El medicamento no existe.");
        }
        return movimientoRepository.findByMedicamento_IdOrderByFechaMovimientoDescIdDesc(medicamentoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarTodos() {
        validarConsultaMovimientos();
        return movimientoRepository.findAllByOrderByFechaMovimientoDescIdDesc();
    }

    /** Limita el historial de movimientos a administración y despacho médico. */
    private void validarConsultaMovimientos() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        boolean permitido = autenticacion != null && autenticacion.isAuthenticated()
                && autenticacion.getAuthorities().stream().map(authority -> normalizarPerfil(authority.getAuthority()))
                .anyMatch(perfil -> List.of("ROLE_ADMIN", "ROLE_ADMINISTRADOR", "ROLE_DESPACHO_MEDICO")
                        .contains(perfil));
        if (!permitido) {
            throw new BadRequestException("El perfil no tiene permiso para consultar movimientos de inventario.");
        }
    }

    /** Normaliza el nombre de perfil eliminando tildes y espacios. */
    private String normalizarPerfil(String nombre) {
        return java.text.Normalizer.normalize(nombre == null ? "" : nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
