package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.TarifaApi;
import com.uniminuto.clinica.models.ImpuestoRq;
import com.uniminuto.clinica.models.TarifaRq;
import com.uniminuto.clinica.models.TarifaRs;
import com.uniminuto.clinica.service.TarifaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Recibe las solicitudes sobre tarifas e IVA y transmite al servicio el usuario responsable de
 * cada cambio.
 */
@RestController
@RequiredArgsConstructor
public class TarifaApiController implements TarifaApi {

    private final TarifaService tarifas;

    @Override
    public List<TarifaRs> listar(Long especializacionId) {
        return tarifas.listar(especializacionId);
    }

    @Override
    public TarifaRs crear(TarifaRq solicitud, Principal usuario) {
        return tarifas.crear(solicitud, Long.parseLong(usuario.getName()));
    }

    @Override
    public TarifaRs actualizar(long id, TarifaRq solicitud, Principal usuario) {
        return tarifas.actualizar(id, solicitud, Long.parseLong(usuario.getName()));
    }

    @Override
    public List<Map<String, Object>> historial(long id) {
        return tarifas.historial(id);
    }

    @Override
    public void configurarIva(long id, ImpuestoRq solicitud, Principal usuario) {
        tarifas.configurarIvaMedicamento(id, solicitud == null ? null : solicitud.ivaPorcentaje(),
                Long.parseLong(usuario.getName()));
    }
}
