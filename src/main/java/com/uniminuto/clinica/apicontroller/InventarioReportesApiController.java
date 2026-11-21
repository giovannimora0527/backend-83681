package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/inventario/reportes")
public class InventarioReportesApiController {
    private final MedicamentoRepository repo;
    public InventarioReportesApiController(MedicamentoRepository repo) { this.repo = repo; }

    public record Resumen(Long id, String codigo, String nombre, String tipoProducto,
                          Integer stock, Integer minimo, String estado, LocalDate vencimiento,
                          java.math.BigDecimal precioCompra, java.math.BigDecimal precioVenta) {}

    @GetMapping("/estado")
    public List<Resumen> estado(@RequestParam(defaultValue="30") int dias) {
        if (dias < 0 || dias > 3650) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,"Dias fuera de rango");
        LocalDate hoy = LocalDate.now();
        return repo.findAll().stream().map(m -> {
            int stock = m.getStockActual() == null ? 0 : m.getStockActual();
            int minimo = m.getStockMinimo() == null ? 0 : m.getStockMinimo();
            String estado = stock == 0 ? "AGOTADO" : stock <= minimo ? "BAJO" : "DISPONIBLE";
            if (m.getFechaVence() != null && m.getFechaVence().isBefore(hoy)) estado = "VENCIDO";
            else if (m.getFechaVence() != null && !m.getFechaVence().isAfter(hoy.plusDays(dias))) estado = "PROXIMO_VENCER";
            return new Resumen(m.getId(),m.getCodigo(),m.getNombre(),m.getTipoProducto(),
                stock,minimo,estado,m.getFechaVence(),m.getPrecioCompra(),m.getPrecioVenta());
        }).toList();
    }

    private static String csv(Object value) {
        String s = value == null ? "" : value.toString();
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    @GetMapping(value="/estado.csv", produces="text/csv")
    public ResponseEntity<byte[]> descargar(@RequestParam(defaultValue="30") int dias) {
        StringBuilder sb = new StringBuilder("id,codigo,nombre,tipo_producto,stock,stock_minimo,estado,fecha_vence,precio_compra,precio_venta\r\n");
        for (Resumen r : estado(dias)) {
            Object[] campos={r.id(),r.codigo(),r.nombre(),r.tipoProducto(),r.stock(),r.minimo(),r.estado(),r.vencimiento(),r.precioCompra(),r.precioVenta()};
            for(int i=0;i<campos.length;i++) { if(i>0) sb.append(','); sb.append(csv(campos[i])); }
            sb.append("\r\n");
        }
        byte[] contenido=("\uFEFF"+sb).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=estado_inventario.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(contenido);
    }
}
