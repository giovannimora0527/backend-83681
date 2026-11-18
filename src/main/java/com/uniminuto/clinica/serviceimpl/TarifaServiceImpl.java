package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.TarifaRq;
import com.uniminuto.clinica.models.TarifaRs;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.TarifaRepository;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.service.TarifaService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aplica las reglas de vigencia, valores e IVA. Conserva los cambios de tarifa y el usuario
 * responsable.
 */
@Service
@RequiredArgsConstructor
public class TarifaServiceImpl implements TarifaService {
    private static final String NOMBRE_CONSULTA_GENERAL = "Consulta general";

    private final TarifaRepository tarifas;
    private final EspecializacionRepository especializaciones;
    private final CitaService citas;

    @Override
    public List<TarifaRs> listar(Long especializacionId) {
        return tarifas.listar(especializacionId);
    }

    @Override
    public List<TarifaRs> listarParaCita(long citaId) {
        return tarifas.listarVigentesParaMedico(citas.detalle(citaId).medicoId());
    }

    @Override
    @Transactional
    public TarifaRs crear(TarifaRq solicitud, long usuarioId) {
        validar(solicitud);
        long id;
        try {
            id = tarifas.insertar(convertir(0, solicitud));
        } catch (DuplicateKeyException ex) {
            throw new BadRequestException("Ya existe una tarifa general. Modifica esa tarifa desde el catálogo.");
        }
        tarifas.registrar(id, "creacion", null, "Registrada", usuarioId);
        return convertir(id, solicitud);
    }

    @Override
    @Transactional
    public TarifaRs actualizar(long id, TarifaRq solicitud, long usuarioId) {
        validar(solicitud);
        var anterior = tarifas.bloquearPorId(id)
                .orElseThrow(() -> new BadRequestException("La tarifa no existe."));
        if (anterior.especializacionId() == null && solicitud.especializacionId() != null) {
            throw new BadRequestException("La tarifa de Consulta general no se puede asociar a una especialización.");
        }
        var nueva = convertir(id, solicitud);
        try {
            tarifas.actualizar(nueva);
        } catch (DuplicateKeyException ex) {
            throw new BadRequestException("Ya existe una tarifa general. Modifica esa tarifa desde el catálogo.");
        }
        registrar(id, "especializacion_id", anterior.especializacionId(), nueva.especializacionId(), usuarioId);
        registrar(id, "nombre_servicio", anterior.nombreServicio(), nueva.nombreServicio(), usuarioId);
        registrar(id, "valor_base", anterior.valorBase(), nueva.valorBase(), usuarioId);
        registrar(id, "iva_porcentaje", anterior.ivaPorcentaje(), nueva.ivaPorcentaje(), usuarioId);
        registrar(id, "activo", anterior.activo(), nueva.activo(), usuarioId);
        registrar(id, "fecha_vigencia", anterior.fechaVigencia(), nueva.fechaVigencia(), usuarioId);
        return nueva;
    }

    @Override
    public List<Map<String, Object>> historial(long id) {
        return tarifas.historial(id);
    }

    @Override
    @Transactional
    public void configurarIvaMedicamento(long id, BigDecimal porcentaje, long usuarioId) {
        if (!ivaValido(porcentaje)) {
            throw new BadRequestException("El porcentaje de IVA debe estar entre 0 y 100 y tener hasta dos decimales.");
        }
        if (!tarifas.existeMedicamento(id)) {
            throw new BadRequestException("El medicamento no existe.");
        }
        tarifas.configurarIvaMedicamento(id, porcentaje, usuarioId);
    }

    private void validar(TarifaRq solicitud) {
        if (solicitud == null
                || solicitud.nombreServicio() == null || solicitud.nombreServicio().isBlank()
                || solicitud.nombreServicio().strip().length() > 150 || solicitud.activo() == null
                || solicitud.fechaVigencia() == null || solicitud.valorBase() == null
                || solicitud.valorBase().signum() < 0 || solicitud.valorBase().scale() > 2
                || solicitud.valorBase().compareTo(new BigDecimal("9999999999.99")) > 0
                || !ivaValido(solicitud.ivaPorcentaje())) {
            throw new BadRequestException("Completa los datos de la tarifa. Revisa el nombre, el valor, el IVA y la vigencia.");
        }
        if (solicitud.especializacionId() != null && !especializaciones.existsById(solicitud.especializacionId())) {
            throw new BadRequestException("La especialización no existe.");
        }
        if (solicitud.especializacionId() == null && !NOMBRE_CONSULTA_GENERAL.equals(solicitud.nombreServicio().strip())) {
            throw new BadRequestException("La tarifa sin especialización se debe llamar Consulta general.");
        }
    }

    private boolean ivaValido(BigDecimal porcentaje) {
        return porcentaje != null && porcentaje.signum() >= 0 && porcentaje.scale() <= 2
                && porcentaje.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    private void registrar(long id, String campo, Object anterior, Object nuevo, long usuarioId) {
        if (!Objects.equals(anterior, nuevo)) {
            tarifas.registrar(id, campo, Objects.toString(anterior, null), Objects.toString(nuevo, null), usuarioId);
        }
    }

    private TarifaRs convertir(long id, TarifaRq solicitud) {
        return new TarifaRs(id, solicitud.especializacionId(), solicitud.nombreServicio().strip(),
                solicitud.valorBase().setScale(2), solicitud.ivaPorcentaje().setScale(2),
                solicitud.activo(), solicitud.fechaVigencia());
    }
}
