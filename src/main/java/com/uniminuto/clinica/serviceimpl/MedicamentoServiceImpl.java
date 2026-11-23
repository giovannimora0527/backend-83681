package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.TipoProducto;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
<<<<<<< HEAD
=======
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Implementación de la lógica de negocio del catálogo de medicamentos.
 */
@Service
public class MedicamentoServiceImpl implements MedicamentoService {

<<<<<<< HEAD
    private static final Pattern CODIGO_VALIDO = Pattern.compile("^[A-Za-z0-9]{8}$");
    private final MedicamentoRepository medicamentoRepository;

    public MedicamentoServiceImpl(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarMedicamentos(String busqueda, String tipoProducto) throws BadRequestException {
        String filtro = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);
        TipoProducto tipo = parsearTipoOpcional(tipoProducto);
        return medicamentoRepository.findAll().stream()
                .filter(medicamento -> tipo == null || tipo.equals(medicamento.getTipoProducto()))
                .filter(medicamento -> filtro.isEmpty()
                        || contiene(medicamento.getNombre(), filtro)
                        || contiene(medicamento.getCodigo(), filtro)
                        || contiene(medicamento.getPresentacion(), filtro))
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .toList();
    }

    @Override
    @Transactional
    public Medicamento guardarMedicamento(MedicamentoRq request) throws BadRequestException {
        TipoProducto tipo = validar(request);
        if (medicamentoRepository.existsByCodigoIgnoreCase(request.getCodigo().trim())) {
            throw new BadRequestException("Ya existe un producto con ese código.");
        }
        if (medicamentoRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new BadRequestException("Ya existe un producto con ese nombre.");
        }

        Medicamento nuevo = new Medicamento();
        aplicar(nuevo, request, tipo);
        return medicamentoRepository.save(nuevo);
    }

    @Override
    @Transactional
    public Medicamento actualizarMedicamento(MedicamentoRq request) throws BadRequestException {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("El identificador del producto es obligatorio.");
        }
        TipoProducto tipo = validar(request);
        Medicamento existente = medicamentoRepository.findById(request.getId())
                .orElseThrow(() -> new BadRequestException("El producto solicitado no existe."));
        if (medicamentoRepository.existsByCodigoIgnoreCaseAndIdNot(request.getCodigo().trim(), existente.getId())) {
            throw new BadRequestException("Ya existe un producto con ese código.");
        }
        if (medicamentoRepository.existsByNombreIgnoreCaseAndIdNot(request.getNombre().trim(), existente.getId())) {
            throw new BadRequestException("Ya existe un producto con ese nombre.");
        }

        aplicar(existente, request, tipo);
        return medicamentoRepository.save(existente);
    }

    private TipoProducto validar(MedicamentoRq request) {
        if (request == null) {
            throw new BadRequestException("Los datos del producto son obligatorios.");
        }
        if (request.getCodigo() == null || !CODIGO_VALIDO.matcher(request.getCodigo().trim()).matches()) {
            throw new BadRequestException("El código debe tener exactamente 8 caracteres alfanuméricos.");
=======
    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public List<Medicamento> listarMedicamentos() {
        return medicamentoRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public Medicamento buscarMedicamentoPorNombre(String nombre) throws BadRequestException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre del medicamento no puede estar vacío");
        }
        return medicamentoRepository.findByNombre(nombre.trim())
                .orElseThrow(() -> new BadRequestException("No se encontró el medicamento: " + nombre, HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(medicamentoRq);

        // Paso 2. Evito medicamentos duplicados
        if (medicamentoRepository.findByNombre(medicamentoRq.getNombre().trim()).isPresent()) {
            throw new BadRequestException("Ya existe un medicamento con el nombre " + medicamentoRq.getNombre(), HttpStatus.CONFLICT);
        }

        // Paso 3. Creo la entidad a guardar
        Medicamento medicamento = new Medicamento();
        this.cargarDatos(medicamento, medicamentoRq);
        this.medicamentoRepository.save(medicamento);

        // Paso 4. Retorno la respuesta
        return respuesta("Medicamento guardado correctamente");
    }

    @Override
    public MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(medicamentoRq);
        if (medicamentoRq.getId() == null || medicamentoRq.getId() <= 0) {
            throw new BadRequestException("El ID del medicamento no puede estar vacío ni ser menor o igual a cero");
        }

        // Paso 2. Verifico que exista y que el nombre no pertenezca a otro
        Medicamento medicamento = this.medicamentoRepository.findById(medicamentoRq.getId())
                .orElseThrow(() -> new BadRequestException("El medicamento con ID " + medicamentoRq.getId() + " no existe", HttpStatus.NOT_FOUND));
        if (medicamentoRepository.findByNombre(medicamentoRq.getNombre().trim())
                .filter(otro -> !otro.getId().equals(medicamento.getId())).isPresent()) {
            throw new BadRequestException("Ya existe otro medicamento con el nombre " + medicamentoRq.getNombre(), HttpStatus.CONFLICT);
        }

        // Paso 3. Actualizo los datos
        this.cargarDatos(medicamento, medicamentoRq);
        this.medicamentoRepository.save(medicamento);

        // Paso 4. Retorno la respuesta
        return respuesta("Medicamento actualizado correctamente");
    }

    private void cargarDatos(Medicamento medicamento, MedicamentoRq rq) {
        medicamento.setNombre(rq.getNombre().trim());
        medicamento.setDescripcion(rq.getDescripcion());
        medicamento.setPresentacion(rq.getPresentacion());
        medicamento.setFechaCompra(rq.getFechaCompra());
        medicamento.setFechaVence(rq.getFechaVence());
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(MedicamentoRq rq) throws BadRequestException {
        if (rq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (rq.getNombre() == null || rq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del medicamento es obligatorio");
        }
        if (rq.getFechaCompra() == null || rq.getFechaVence() == null) {
            throw new BadRequestException("La fecha de compra y la de vencimiento son obligatorias");
        }
        if (rq.getFechaVence().isBefore(rq.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la fecha de compra");
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
        }
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del producto es obligatorio.");
        }
        if (request.getPresentacion() == null || request.getPresentacion().isBlank()) {
            throw new BadRequestException("La presentación del producto es obligatoria.");
        }
        if (request.getPrecioCompra() == null || request.getPrecioCompra().signum() < 0
                || request.getPrecioPublico() == null || request.getPrecioPublico().signum() < 0) {
            throw new BadRequestException("Los precios son obligatorios y no pueden ser negativos.");
        }
        if (request.getStockMinimo() == null || request.getStockMinimo() < 0) {
            throw new BadRequestException("El stock mínimo es obligatorio y no puede ser negativo.");
        }

        TipoProducto tipo = parsearTipoOpcional(request.getTipoProducto());
        if (tipo == null) {
            throw new BadRequestException("El tipo debe ser MEDICAMENTO o INSUMO.");
        }
        if (tipo == TipoProducto.MEDICAMENTO
                && (request.getRegistroInvima() == null || request.getRegistroInvima().isBlank())) {
            throw new BadRequestException("El registro Invima es obligatorio para los medicamentos.");
        }
        return tipo;
    }

    private TipoProducto parsearTipoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return TipoProducto.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El tipo debe ser MEDICAMENTO o INSUMO.");
        }
    }

    private void aplicar(Medicamento medicamento, MedicamentoRq request, TipoProducto tipo) {
        medicamento.setCodigo(request.getCodigo().trim().toUpperCase(Locale.ROOT));
        medicamento.setTipoProducto(tipo);
        medicamento.setNombre(request.getNombre().trim());
        medicamento.setDescripcion(limpiar(request.getDescripcion()));
        medicamento.setPresentacion(request.getPresentacion().trim());
        medicamento.setRegistroInvima(limpiar(request.getRegistroInvima()));
        medicamento.setPrecioCompra(request.getPrecioCompra().setScale(2, java.math.RoundingMode.HALF_UP));
        medicamento.setPrecioPublico(request.getPrecioPublico().setScale(2, java.math.RoundingMode.HALF_UP));
        medicamento.setStockMinimo(request.getStockMinimo());
        if (request.getActivo() != null) {
            medicamento.setActivo(request.getActivo());
        } else if (medicamento.getId() == null) {
            medicamento.setActivo(true);
        }
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }
}
