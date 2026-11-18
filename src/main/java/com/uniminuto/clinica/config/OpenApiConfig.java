package com.uniminuto.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Configura la descripción de la API para poder consultar las rutas y usar el token de sesión en
 * Swagger.
 */
@Configuration
public class OpenApiConfig {

    private static final String PDF = "application/pdf";
    private static final String EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Bean
    OpenAPI apiClinica() {
        return new OpenAPI()
                .info(new Info().title("Clínica veterinaria Uniminuto").version("1")
                        .description("""
                                Consulta las operaciones por módulo y sus permisos.
                                Para probar las rutas protegidas, ejecuta el login, valida el OTP recibido
                                por correo y usa el JWT de la segunda respuesta en Authorize.
                                """))
                .components(new Components().addSecuritySchemes("jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("jwt"));
    }

    @Bean
    OpenApiCustomizer documentarRespuestasClinica() {
        return api -> api.getPaths().forEach((ruta, recurso) ->
                recurso.readOperationsMap().forEach((metodo, operacion) -> documentar(ruta, metodo, operacion)));
    }

    private void documentar(String ruta, PathItem.HttpMethod metodo, Operation operacion) {
        var respuestas = operacion.getResponses();
        if (respuestas == null) {
            respuestas = new ApiResponses();
            operacion.setResponses(respuestas);
        }
        respuestas.putIfAbsent("400", new ApiResponse().description("Solicitud inválida o regla de negocio incumplida."));
        boolean accesoPublico = ruta.equals("/auth/login") || ruta.equals("/auth/validate-token");
        if (accesoPublico) {
            operacion.setSecurity(List.of());
        } else {
            respuestas.putIfAbsent("401", new ApiResponse().description("Se requiere un JWT válido."));
            if (!ruta.equals("/auth/cerrar-sesion")) {
                respuestas.putIfAbsent("403", new ApiResponse().description("La cuenta no tiene permiso para esta operación."));
            }
        }

        if (metodo == PathItem.HttpMethod.GET) {
            documentarDescarga(ruta, respuestas);
        }
        documentarEstadoExitoso(ruta, metodo, respuestas);
        documentarParametros(ruta, operacion);
        documentarEjemploSolicitud(ruta, operacion);
    }

    private void documentarDescarga(String ruta, ApiResponses respuestas) {
        var contenido = new Content();
        if (ruta.equals("/reportes/medicos/{formato}") || ruta.equals("/reportes/financieros/{formato}")) {
            contenido.addMediaType(PDF, archivo()).addMediaType(EXCEL, archivo());
        } else if (ruta.endsWith("/pdf") || ruta.endsWith("/exportar-pdf") || ruta.endsWith("/comprobante")) {
            contenido.addMediaType(PDF, archivo());
        } else if (ruta.endsWith("/excel") || ruta.endsWith("/exportar-excel")) {
            contenido.addMediaType(EXCEL, archivo());
        }
        if (!contenido.isEmpty()) {
            respuestas.addApiResponse("200", new ApiResponse().description("Archivo disponible para descargar.")
                    .content(contenido));
        }
    }

    private MediaType archivo() {
        return new MediaType().schema(new StringSchema().format("binary"));
    }

    private void documentarEstadoExitoso(String ruta, PathItem.HttpMethod metodo, ApiResponses respuestas) {
        boolean sinContenido = ruta.equals("/factura/{id}/anular")
                || ruta.equals("/factura/{id}/pagos/{pagoId}/devolucion")
                || ruta.equals("/tarifa/impuestos-medicamento/{id}")
                || ruta.equals("/medico/{medicoId}/cuenta") && metodo == PathItem.HttpMethod.DELETE;
        if (sinContenido) {
            respuestas.remove("200");
            respuestas.addApiResponse("204", new ApiResponse().description("Operación completada sin cuerpo de respuesta."));
        } else if (ruta.equals("/medico/{medicoId}/cuenta") && metodo == PathItem.HttpMethod.GET) {
            respuestas.addApiResponse("204", new ApiResponse().description("El veterinario todavía no tiene una cuenta vinculada."));
        }
    }

    private void documentarParametros(String ruta, Operation operacion) {
        if (operacion.getParameters() == null) {
            return;
        }
        for (var parametro : operacion.getParameters()) {
            switch (parametro.getName()) {
                case "periodo" -> parametro.description("Agrupación del reporte por día, mes o año.").example("mes");
                case "formato" -> parametro.description("Formato de descarga. Valores admitidos: pdf y excel.").example("pdf");
                case "especializacionId" -> parametro.description("Identificador de la especialidad.").example(16);
                case "medicoId" -> parametro.description("Identificador del veterinario.").example(14);
                case "citaId" -> parametro.description("Identificador de la cita.").example(9);
                case "fechaInicio", "fechaFin" -> parametro.description("Fecha y hora ISO 8601 en horario de Bogotá.")
                        .example(parametro.getName().equals("fechaInicio") ? "2027-01-18T00:00:00" : "2027-01-19T00:00:00");
                case "inicio", "fin" -> {
                    boolean agendaConHora = ruta.equals("/cita/calendario");
                    String fecha = parametro.getName().equals("inicio") ? "2027-01-18" : "2027-01-19";
                    parametro.example(agendaConHora ? fecha + "T00:00:00" : fecha);
                }
                case "tipo" -> {
                    if (ruta.contains("/informes")) {
                        parametro.description("TODOS, BAJO, AGOTADO, PROXIMO_VENCER, VENCIDO, ENTRADAS o SALIDAS.")
                                .example("TODOS");
                    }
                }
                default -> {
                    // Se conserva la descripción obtenida del contrato de Spring.
                }
            }
        }
    }

    private void documentarEjemploSolicitud(String ruta, Operation operacion) {
        if (operacion.getRequestBody() == null || operacion.getRequestBody().getContent() == null) {
            return;
        }
        var json = operacion.getRequestBody().getContent().get("application/json");
        if (json == null) {
            return;
        }
        switch (ruta) {
            case "/cita/actualizar" -> json.example(Map.of("id", 9, "estado", "CANCELADA",
                    "motivoCancelacion", "El propietario solicitó la cancelación"));
            case "/cliente/crear", "/cliente/guardar" -> json.example(Map.of(
                    "tipoDocumento", "CC", "numeroDocumento", "9000000740", "nombres", "Valeria",
                    "apellidos", "Rojas", "fechaNacimiento", "1990-04-15", "activo", true));
            case "/cliente/actualizar" -> json.example(Map.of("id", 11, "tipoDocumento", "CC",
                    "numeroDocumento", "9000000702", "nombres", "Daniel", "apellidos", "Mendoza",
                    "fechaNacimiento", "1993-05-12", "activo", true));
            default -> {
                // Los demás ejemplos se definen en el modelo de solicitud.
            }
        }
    }
}
