package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.models.ImportacionFila;
import com.uniminuto.clinica.service.ImportacionFilaProcessor;
import com.uniminuto.clinica.service.InventarioService;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ImportacionFilaProcessorImpl implements ImportacionFilaProcessor {
    private final MedicamentoService productos;
    private final InventarioService inventario;
    public ImportacionFilaProcessorImpl(MedicamentoService productos, InventarioService inventario) { this.productos = productos; this.inventario = inventario; }
    @Override @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesar(ImportacionFila fila, Long usuarioId) {
        var producto = productos.crear(fila.producto());
        var lote = fila.lote();
        inventario.registrarEntrada(new com.uniminuto.clinica.models.EntradaLoteRq(producto.getId(), lote.numeroLote(),
                lote.fechaIngreso(), lote.fechaVencimiento(), lote.cantidad(), lote.precioCompraUnitario(),
                lote.precioVentaUnitario(), lote.referenciaProveedor(), usuarioId));
    }
}
