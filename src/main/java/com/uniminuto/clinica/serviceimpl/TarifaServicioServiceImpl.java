package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.entity.TarifaServicio;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.TarifaServicioRq;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.TarifaServicioRepository;
import com.uniminuto.clinica.service.TarifaServicioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class TarifaServicioServiceImpl implements TarifaServicioService {
    private final TarifaServicioRepository tarifas;
    private final EspecializacionRepository especializaciones;
    public TarifaServicioServiceImpl(TarifaServicioRepository tarifas, EspecializacionRepository especializaciones) {
        this.tarifas = tarifas; this.especializaciones = especializaciones;
    }
    @Override public List<TarifaServicio> listar() { return tarifas.findByVigenteHastaIsNullOrderByNombreAsc(); }
    @Override public List<TarifaServicio> historial(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new BadRequestException("El nombre del servicio es obligatorio.");
        return tarifas.findByNombreOrderByVigenteDesdeDesc(nombre.trim());
    }
    @Override @Transactional public TarifaServicio crear(TarifaServicioRq request) {
        validar(request); return tarifas.save(nueva(request, null));
    }
    @Override @Transactional public TarifaServicio actualizar(Long id, TarifaServicioRq request) {
        validar(request);
        TarifaServicio anterior = tarifas.findLockedById(id).orElseThrow(() -> new BadRequestException("La tarifa no existe."));
        if (anterior.getVigenteHasta() != null) throw new BadRequestException("No puede modificarse una tarifa histórica.");
        LocalDateTime ahora = LocalDateTime.now(); anterior.setVigenteHasta(ahora); tarifas.save(anterior);
        return tarifas.save(nueva(request, ahora));
    }
    private TarifaServicio nueva(TarifaServicioRq rq, LocalDateTime desde) {
        TarifaServicio t = new TarifaServicio();
        if (rq.especializacionId() != null) t.setEspecializacion(especializaciones.findById(rq.especializacionId())
                .orElseThrow(() -> new BadRequestException("La especialidad no existe.")));
        t.setNombre(rq.nombre().trim()); t.setDescripcion(rq.descripcion()); t.setPrecio(rq.precio());
        t.setIvaPorcentaje(rq.ivaPorcentaje()); t.setActivo(rq.activo() == null || rq.activo());
        t.setVigenteDesde(desde == null ? LocalDateTime.now() : desde); return t;
    }
    private static void validar(TarifaServicioRq rq) {
        if (rq == null || rq.nombre() == null || rq.nombre().isBlank() || rq.precio() == null || rq.ivaPorcentaje() == null)
            throw new BadRequestException("Nombre, precio e IVA son obligatorios.");
        if (rq.precio().compareTo(BigDecimal.ZERO) < 0 || rq.ivaPorcentaje().compareTo(BigDecimal.ZERO) < 0
                || rq.ivaPorcentaje().compareTo(new BigDecimal("100")) > 0)
            throw new BadRequestException("Precio no negativo e IVA entre 0 y 100 son requeridos.");
    }
}
