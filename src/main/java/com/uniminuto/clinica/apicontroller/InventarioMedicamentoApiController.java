package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/inventario/medicamentos")
public class InventarioMedicamentoApiController {
    private final MedicamentoRepository repositorio;
    public InventarioMedicamentoApiController(MedicamentoRepository repositorio) { this.repositorio = repositorio; }
    @GetMapping
    public List<Medicamento> listar() { return repositorio.findAll(); }
    @GetMapping("/{id}")
    public Medicamento obtener(@PathVariable Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }
    @PostMapping
    public ResponseEntity<Medicamento> crear(@RequestBody Medicamento datos) {
        validar(datos);
        if (repositorio.existsByCodigo(datos.getCodigo()) || repositorio.existsByNombre(datos.getNombre()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Codigo o nombre duplicado");
        datos.setId(null);
        datos.setStockActual(0);
        datos.setFechaCreacionRegistro(null);
        datos.setFechaModificacionRegistro(null);
        datos.setRequiereInvima("MEDICAMENTO".equalsIgnoreCase(datos.getTipoProducto()));
        return ResponseEntity.status(HttpStatus.CREATED).body(repositorio.save(datos));
    }
    @PutMapping("/{id}")
    public Medicamento editar(@PathVariable Long id, @RequestBody Medicamento datos) {
        Medicamento actual = obtener(id);
        validar(datos);
        if (!actual.getCodigo().equals(datos.getCodigo()) && repositorio.existsByCodigo(datos.getCodigo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Codigo duplicado");
        if (!actual.getNombre().equals(datos.getNombre()) && repositorio.existsByNombre(datos.getNombre()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre duplicado");
        actual.setCodigo(datos.getCodigo()); actual.setNombre(datos.getNombre());
        actual.setDescripcion(datos.getDescripcion()); actual.setPresentacion(datos.getPresentacion());
        actual.setTipoProducto(datos.getTipoProducto()); actual.setRegistroInvima(datos.getRegistroInvima());
        actual.setRequiereInvima("MEDICAMENTO".equalsIgnoreCase(datos.getTipoProducto()));
        actual.setPrecioCompra(datos.getPrecioCompra()); actual.setPrecioVenta(datos.getPrecioVenta());
        actual.setStockMinimo(datos.getStockMinimo()); actual.setFechaCompra(datos.getFechaCompra());
        actual.setFechaVence(datos.getFechaVence()); actual.setFechaIngreso(datos.getFechaIngreso());
        actual.setFechaProximoPedido(datos.getFechaProximoPedido());
        if (datos.getActivo() != null) actual.setActivo(datos.getActivo());
        // El stock nunca se edita desde esta API: se gestiona mediante lotes.
        return repositorio.save(actual);
    }
    private void validar(Medicamento m) {
        if (m.getCodigo() == null || !m.getCodigo().matches("[A-Za-z0-9]{8}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo alfanumerico de 8 caracteres requerido");
        if (m.getNombre() == null || m.getNombre().isBlank() || m.getNombre().length() > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre invalido");
        if (m.getTipoProducto() == null || m.getTipoProducto().isBlank() || m.getTipoProducto().length() > 20)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de producto invalido");
        if ("MEDICAMENTO".equalsIgnoreCase(m.getTipoProducto()) && (m.getRegistroInvima() == null || m.getRegistroInvima().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVIMA obligatorio para medicamentos");
        if (m.getFechaCompra() == null || m.getFechaVence() == null || m.getFechaVence().isBefore(m.getFechaCompra()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fechas invalidas");
        if (m.getPrecioCompra() == null || m.getPrecioVenta() == null || m.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0 || m.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precios invalidos");
        if (m.getStockMinimo() == null || m.getStockMinimo() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock minimo invalido");
    }
}
