package com.uniminuto.clinica.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

/** Describe en Swagger el uso de cada operación desde la interfaz de Angular. */
final class ConsumoAngularOpenApi {

    private static final Map<String, String> CONSUMOS = Map.ofEntries(
            Map.entry("POST /auth/login", "Inicio de sesión. Envía usuario y contraseña para solicitar el código de verificación."),
            Map.entry("POST /auth/validate-token", "Inicio de sesión. Envía el código de verificación y guarda el JWT devuelto para autenticar las peticiones siguientes."),
            Map.entry("GET /cita/listar", "Citas, Fórmulas y Facturación. Carga las citas para el listado y los selectores relacionados."),
            Map.entry("GET /cita/calendario", "Mi agenda. Envía el inicio y el fin del período visible para cargar las citas del veterinario."),
            Map.entry("GET /cita/calendario/excel", "Mi agenda. Envía el rango elegido y descarga el Excel como archivo binario."),
            Map.entry("GET /cita/listar-por-mascota", "Citas y Mi agenda. Envía el identificador de la mascota y presenta su historial de citas."),
            Map.entry("POST /cita/crear", "Citas y Mi agenda. Envía los datos del formulario y muestra el resultado o el motivo del rechazo."),
            Map.entry("POST /cita/actualizar", "Citas y Mi agenda. Envía los cambios de una cita existente."),
            Map.entry("PATCH /cita/{id}/reprogramar", "Mi agenda. Envía la nueva fecha y hora de la cita y vuelve a consultar el calendario."),
            Map.entry("GET /cliente/listar", "Clientes, Mascotas, Citas y Facturación. Carga los propietarios para la tabla o los selectores."),
            Map.entry("GET /mascota/listar", "Mascotas, Mi agenda y Facturación. Carga las mascotas y permite filtrarlas en la pantalla."),
            Map.entry("POST /mascota/guardar", "Mascotas. Registra el paciente con los datos del formulario."),
            Map.entry("POST /mascota/actualizar", "Mascotas. Guarda cambios en un paciente existente."),
            Map.entry("GET /raza/listar", "Mascotas. Carga las opciones de raza para el formulario."),
            Map.entry("GET /medico/listar", "Médicos, Citas, Historias y Reportes. Carga los veterinarios para listados y selectores."),
            Map.entry("GET /medico/listar-disponibles", "Citas. Envía horario, duración y mascota según corresponda para consultar veterinarios disponibles."),
            Map.entry("POST /medico/crear", "Médicos. Envía el formulario de registro del veterinario."),
            Map.entry("PUT /medico/actualizar", "Médicos. Guarda los cambios del veterinario seleccionado."),
            Map.entry("GET /especializacion/listar", "Especializaciones, Médicos, Tarifas y Reportes. Carga las especialidades para tablas y filtros."),
            Map.entry("POST /especializacion/crear", "Especialidades y tarifas. Envía los datos de una especialidad nueva."),
            Map.entry("PUT /especializacion/actualizar", "Especialidades y tarifas. Guarda cambios en los datos o el estado de la especialidad."),
            Map.entry("GET /tarifa", "Especialidades y tarifas. Lista las tarifas y permite filtrar por especialidad."),
            Map.entry("POST /tarifa", "Especialidades y tarifas. Envía el nombre, precio, IVA y vigencia del servicio."),
            Map.entry("PUT /tarifa/{id}", "Especialidades y tarifas. Guarda los cambios de la tarifa elegida."),
            Map.entry("GET /tarifa/{id}/historial", "Especialidades y tarifas. Consulta los cambios registrados para la tarifa."),
            Map.entry("GET /tarifa/impuestos-medicamento/{id}", "Especialidades y tarifas. Carga el IVA vigente del medicamento elegido."),
            Map.entry("PUT /tarifa/impuestos-medicamento/{id}", "Especialidades y tarifas. Guarda el IVA configurado para el medicamento."),
            Map.entry("GET /historia-clinica/listar", "Historias clínicas. Carga la información clínica disponible para el perfil autenticado."),
            Map.entry("POST /historia-clinica/guardar", "Historias clínicas. Registra la historia clínica desde el formulario."),
            Map.entry("PUT /historia-clinica/actualizar", "Historias clínicas. Envía cambios en la historia seleccionada."),
            Map.entry("GET /anotacion-historia/listar", "Historias clínicas. Carga las anotaciones clínicas del listado."),
            Map.entry("POST /anotacion-historia/crear", "Historias clínicas. Agrega una anotación."),
            Map.entry("PUT /anotacion-historia/actualizar", "Historias clínicas. Guarda cambios en una anotación."),
            Map.entry("GET /formula-medica/listar", "Fórmulas y Atención. Carga las fórmulas y, en Atención, identifica las de la cita abierta."),
            Map.entry("POST /formula-medica/guardar", "Gestión de fórmulas. Envía la prescripción desde el formulario."),
            Map.entry("POST /formula-medica/actualizar", "Gestión de fórmulas. Envía cambios de la fórmula seleccionada."),
            Map.entry("GET /atencion/{citaId}", "Atención médica. Abre la cita y muestra antecedentes, datos clínicos y documentos relacionados."),
            Map.entry("GET /atencion/{citaId}/tarifas", "Atención médica. Carga los servicios que se pueden añadir al cierre de la cita."),
            Map.entry("GET /atencion/{citaId}/existencias/{medicamentoId}", "Atención médica. Consulta el saldo dispensable del producto al elegirlo para una fórmula."),
            Map.entry("POST /atencion/{citaId}/formulas", "Atención médica. Envía la prescripción y sus medicamentos para guardarlos en la cita."),
            Map.entry("POST /atencion/{citaId}/finalizar", "Atención médica. Envía el registro clínico y los servicios elegidos para cerrar la cita y generar la prefactura."),
            Map.entry("GET /atencion/{citaId}/formulas/{formulaId}/pdf", "Atención médica. Solicita el PDF autenticado para verlo, imprimirlo o descargarlo."),
            Map.entry("GET /atencion/{citaId}/prefactura/{id}/pdf", "Atención médica. Descarga la prefactura generada al cerrar la cita."),
            Map.entry("GET /factura/historial", "Facturación. Consulta facturas y permite filtrar por propietario y mascota."),
            Map.entry("GET /factura/cita/{citaId}/estado", "Facturación. Indica si la cita permite emitir una factura y muestra el motivo cuando está bloqueada."),
            Map.entry("POST /factura/cita/{citaId}", "Facturación. Solicita la emisión de la factura con el descuento autorizado, si aplica."),
            Map.entry("GET /factura/{id}/pagos", "Facturación. Carga los pagos registrados y las devoluciones de la factura."),
            Map.entry("POST /factura/{id}/pagos", "Facturación. Envía el importe, el medio de pago y la observación."),
            Map.entry("POST /factura/{id}/pagos/{pagoId}/devolucion", "Facturación. Envía la referencia para devolver un pago."),
            Map.entry("POST /factura/{id}/anular", "Facturación. Envía el motivo de la anulación."),
            Map.entry("GET /factura/{id}/pdf", "Facturación. Descarga el PDF de la factura seleccionada."),
            Map.entry("GET /medicamento/listar", "Inventario, Fórmulas y Tarifas. Consulta el catálogo para la tabla y los selectores."),
            Map.entry("GET /medicamento/disponibilidad", "Medicamentos, Inventario, Atención y Despacho. Carga los productos disponibles para la consulta del perfil."),
            Map.entry("POST /medicamento/crear", "Inventario. Registra un producto con sus precios y existencias iniciales."),
            Map.entry("PUT /medicamento/actualizar", "Inventario. Guarda los cambios de un producto."),
            Map.entry("GET /inventario-lote/listar", "Inventario. Carga los lotes para mostrar sus cantidades, costos y vencimientos."),
            Map.entry("POST /inventario-lote/guardar", "Inventario. Registra un lote con su cantidad, costo y vencimiento cuando aplica."),
            Map.entry("GET /movimiento-inventario/listar", "Inventario. Muestra el historial general de entradas, salidas y otros movimientos."),
            Map.entry("GET /medicamento/{medicamentoId}/movimientos", "Inventario. Consulta los movimientos al seleccionar un producto."),
            Map.entry("POST /inventario/carga-masiva/previsualizar", "Inventario. Envía un CSV o XLSX como archivo y muestra los registros válidos y los errores."),
            Map.entry("POST /inventario/carga-masiva/{cargaId}/confirmar", "Inventario. Confirma la importación de la vista previa y muestra el resultado."),
            Map.entry("GET /medicamento/informes", "Inventario. Obtiene la vista previa con el tipo y el rango de fechas seleccionados."),
            Map.entry("GET /medicamento/informes/exportar-pdf", "Inventario. Descarga el PDF con los filtros usados en la vista previa."),
            Map.entry("GET /medicamento/informes/exportar-excel", "Inventario. Descarga el Excel con los filtros usados en la vista previa."),
            Map.entry("GET /inventario/dispensaciones/formulas-pendientes", "Despacho. Busca fórmulas pendientes por texto o fecha de generación."),
            Map.entry("GET /inventario/dispensaciones/formula/{formulaId}/preparacion", "Despacho. Consulta las cantidades y los lotes para preparar la entrega."),
            Map.entry("POST /inventario/dispensaciones/preparar", "Despacho. Inicia la preparación de la fórmula antes de registrar salidas."),
            Map.entry("POST /inventario/dispensaciones/{despachoId}/confirmar", "Despacho. Envía las cantidades por lote y conserva la clave al reintentar la misma entrega."),
            Map.entry("GET /inventario/dispensaciones/historial", "Despacho. Carga el historial de entregas al abrir la vista o después de registrar un cambio."),
            Map.entry("GET /inventario/dispensaciones/{despachoId}/detalles", "Despacho. Muestra los medicamentos, lotes y movimientos de una entrega."),
            Map.entry("GET /inventario/dispensaciones/{despachoId}/historial-estados", "Despacho. Carga los cambios de estado junto con su fecha y responsable."),
            Map.entry("GET /inventario/dispensaciones/medicamento/{medicamentoId}/historial", "Despacho. Consulta las entregas vinculadas al medicamento elegido."),
            Map.entry("GET /inventario/dispensaciones/{despachoId}/comprobante", "Despacho. Descarga el comprobante PDF de la entrega."),
            Map.entry("POST /inventario/dispensaciones/{despachoId}/cancelar", "Despacho. Envía la justificación para cancelar o revertir la entrega."),
            Map.entry("GET /reportes/medicos", "Reportes. Envía fechas y filtros de veterinario y especialidad para mostrar la vista previa."),
            Map.entry("GET /reportes/medicos/{formato}", "Reportes. Descarga el PDF o el Excel con los filtros de la consulta médica."),
            Map.entry("GET /reportes/financieros", "Reportes. Envía fechas, agrupación y filtros para mostrar el resumen financiero."),
            Map.entry("GET /reportes/financieros/{formato}", "Reportes. Descarga el PDF o el Excel con los filtros de la consulta financiera."),
            Map.entry("GET /usuario/listar", "Usuarios. Carga las cuentas que se muestran en el listado."),
            Map.entry("POST /usuario/actualizar", "Usuarios. Envía los cambios de la cuenta seleccionada.")
    );

    private ConsumoAngularOpenApi() {
    }

    /** Agrega el uso de la operación sin reemplazar la descripción que ya tiene la API. */
    static void documentar(String ruta, PathItem.HttpMethod metodo, Operation operacion) {
        String uso = CONSUMOS.get(metodo.name() + " " + ruta);
        if (uso == null) {
            uso = "Esta operación está disponible en la API. No se identificó una llamada directa desde Angular.";
        }
        String descripcion = operacion.getDescription();
        String seccion = "**Uso desde Angular**\n\n" + uso;
        operacion.setDescription(descripcion == null || descripcion.isBlank()
                ? seccion : descripcion + "\n\n" + seccion);
    }
}

