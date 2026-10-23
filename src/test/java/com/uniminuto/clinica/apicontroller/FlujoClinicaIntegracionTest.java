package com.uniminuto.clinica.apicontroller;

import com.jayway.jsonpath.JsonPath;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración del flujo completo Cliente → Mascota → Cita →
 * Historia clínica → Anotación → Fórmula, llamando a los endpoints reales
 * sobre una base de datos H2 en memoria. Verifica códigos HTTP, filtros,
 * ordenamiento, paginación y reglas de negocio de punta a punta.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlujoClinicaIntegracionTest {

    /**
     * Contexto web de la aplicación, usado para construir MockMvc.
     */
    @Autowired
    private WebApplicationContext contexto;

    @LocalServerPort
    private int puerto;

    /**
     * Repositorios usados para preparar y limpiar los datos.
     */
    @Autowired
    private FormulaMedicaRepository formulaRepository;
    @Autowired
    private AnotacionHistoriaRepository anotacionRepository;
    @Autowired
    private HistoriaMedicaRepository historiaRepository;
    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private MascotaRepository mascotaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private MedicoRepository medicoRepository;
    @Autowired
    private RazaRepository razaRepository;
    @Autowired
    private EspecializacionRepository especializacionRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private MedicamentoRepository medicamentoRepository;

    /**
     * EntityManager y transacciones para insertar las especializaciones.
     */
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TransactionTemplate transaccion;

    /**
     * Cliente HTTP simulado.
     */
    private MockMvc mvc;

    /**
     * Datos de prueba.
     */
    private Cliente juan;
    private Mascota max;
    private Mascota rocky;
    private Medico laura;
    private Cita citaAtendida;

    /**
     * Limpia la base y crea los datos de prueba antes de cada caso.
     */
    @BeforeEach
    void prepararDatos() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        formulaRepository.deleteAll();
        medicamentoRepository.deleteAll();
        usuarioRepository.deleteAll();
        anotacionRepository.deleteAll();
        historiaRepository.deleteAll();
        citaRepository.deleteAll();
        mascotaRepository.deleteAll();
        clienteRepository.deleteAll();
        medicoRepository.deleteAll();
        razaRepository.deleteAll();

        Especializacion especializacion = transaccion.execute(estado -> {
            especializacionRepository.deleteAll();
            Especializacion e = new Especializacion();
            e.setNombre("Medicina general");
            e.setCodigoEspecializacion("MG");
            entityManager.persist(e);
            return e;
        });

        Raza raza = new Raza();
        raza.setNombre("Criollo");
        raza.setEspecie("Perro");
        raza = razaRepository.save(raza);

        juan = crearCliente("Juan", "Pérez", "100");
        Cliente maria = crearCliente("María", "López", "200");
        max = crearMascota("Max", juan, raza);
        rocky = crearMascota("Rocky", maria, raza);

        laura = new Medico();
        laura.setTipoDocumento("CC");
        laura.setNumeroDocumento("80");
        laura.setNombres("Laura");
        laura.setApellidos("Gómez");
        laura.setRegistroProfesional("RP-1");
        laura.setEspecializacion(especializacion);
        laura = medicoRepository.save(laura);

        citaAtendida = new Cita();
        citaAtendida.setMascota(max);
        citaAtendida.setMedico(laura);
        citaAtendida.setFechaCita(LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.HOURS));
        citaAtendida.setMotivo("Vómito");
        citaAtendida.setEstado(EstadoCita.ATENDIDA);
        citaAtendida = citaRepository.save(citaAtendida);
    }

    @Test
    @DisplayName("Razas: lista con CORS para el preflight del frontend local")
    void listarRazasPermitePreflightLocal() throws Exception {
        String origen = "http://localhost:65041";

        mvc.perform(options("/raza/listar")
                        .header("Origin", origen)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", origen));

        mvc.perform(get("/raza/listar").header("Origin", origen))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", origen))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Criollo"));
    }

    @Test
    @DisplayName("Especializaciones: lista con CORS para el frontend local")
    void listarEspecializacionesPermitePreflightLocal() throws Exception {
        String origen = "http://localhost:4200";

        mvc.perform(options("/especializacion/listar")
                        .header("Origin", origen)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", origen));

        mvc.perform(get("/especializacion/listar").header("Origin", origen))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", origen))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Medicina general"));
    }

    @Test
    @DisplayName("Médicos: preflight CORS y creación con especialización existente")
    void crearMedicoPermitePreflightLocalYGuarda() throws Exception {
        String origen = "http://localhost:4200";

        mvc.perform(options("/medico/guardar")
                        .header("Origin", origen)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Methods",
                                org.hamcrest.Matchers.containsString("POST")));

        String cuerpo = """
                {"tipoDocumento":"CC","numeroDocumento":"900",
                 "nombres":"Carlos","apellidos":"Prueba","telefono":"3001234567",
                 "registroProfesional":"RP-900","especializacionId":%d}
                """.formatted(laura.getEspecializacion().getId());

        MvcResult resultado = mvc.perform(post("/medico/guardar")
                        .header("Origin", origen)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(jsonPath("$.message").value("Médico creado correctamente."))
                .andReturn();

        long medicoId = ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
        org.junit.jupiter.api.Assertions.assertTrue(medicoRepository.existsById(medicoId));
    }

    @Test
    @DisplayName("Médicos: listar, consultar, actualizar y eliminar con CORS")
    void operacionesMedicoPermitenPreflightYPersistenCambios() throws Exception {
        String origen = "http://localhost:4200";
        String cuerpo = """
                {"tipoDocumento":"CC","numeroDocumento":"901",
                 "nombres":"Carlos","apellidos":"Prueba","telefono":"3001234567",
                 "registroProfesional":"RP-901","especializacionId":%d}
                """.formatted(laura.getEspecializacion().getId());

        mvc.perform(get("/medico/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/medico/buscar")
                        .param("tipoDocumento", "CC")
                        .param("numeroDocumento", "80")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Laura"));
        mvc.perform(get("/medico/buscarPorRegistroProfesional")
                        .param("registroProfesional", "RP-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDocumento").value("80"));

        MvcResult creada = mvc.perform(post("/medico/guardar")
                        .header("Origin", origen)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn();
        long medicoId = ((Number) JsonPath.read(creada.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(options("/medico/actualizar")
                        .header("Origin", origen)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Methods",
                                org.hamcrest.Matchers.containsString("POST")));

        String actualizacion = """
                {"id":%d,"tipoDocumento":"CC","numeroDocumento":"901",
                 "nombres":"Carlos Andrés","apellidos":"Prueba","telefono":"3001234567",
                 "registroProfesional":"RP-901","especializacionId":%d}
                """.formatted(medicoId, laura.getEspecializacion().getId());
        mvc.perform(post("/medico/actualizar")
                        .header("Origin", origen)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actualizacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Médico actualizado correctamente."));
        org.junit.jupiter.api.Assertions.assertEquals(
                "Carlos Andrés", medicoRepository.findById(medicoId).orElseThrow().getNombres());

        mvc.perform(options("/medico/eliminar/" + medicoId)
                        .header("Origin", origen)
                        .header("Access-Control-Request-Method", "DELETE")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Methods",
                                org.hamcrest.Matchers.containsString("DELETE")));
        mvc.perform(delete("/medico/eliminar/" + medicoId)
                        .header("Origin", origen))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Médico eliminado correctamente."));
        org.junit.jupiter.api.Assertions.assertFalse(medicoRepository.existsById(medicoId));

        mvc.perform(delete("/medico/eliminar/" + laura.getId())
                        .header("Origin", origen))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("citas asociadas")));
    }

    @Test
    @DisplayName("Médicos: preflight DELETE responde con CORS en servidor HTTP real")
    void preflightEliminarMedicoFuncionaSobreHttpReal() throws Exception {
        HttpRequest solicitud = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + puerto + "/clinica/v1/medico/eliminar/999999"))
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "DELETE")
                .header("Access-Control-Request-Headers", "content-type")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> respuesta = HttpClient.newHttpClient()
                .send(solicitud, HttpResponse.BodyHandlers.ofString());

        org.junit.jupiter.api.Assertions.assertEquals(200, respuesta.statusCode());
        org.junit.jupiter.api.Assertions.assertEquals(
                "*", respuesta.headers().firstValue("Access-Control-Allow-Origin").orElse(null),
                () -> "HTTP status: " + respuesta.statusCode() + "; headers: " + respuesta.headers().map()
                        + "; body: " + respuesta.body());
        org.junit.jupiter.api.Assertions.assertTrue(
                respuesta.headers().firstValue("Access-Control-Allow-Methods").orElse("").contains("DELETE"));

        Medico medicoTemporal = new Medico();
        medicoTemporal.setTipoDocumento("CC");
        medicoTemporal.setNumeroDocumento("902");
        medicoTemporal.setNombres("Temporal");
        medicoTemporal.setApellidos("HTTP");
        medicoTemporal.setRegistroProfesional("RP-902");
        medicoTemporal.setEspecializacion(laura.getEspecializacion());
        medicoTemporal = medicoRepository.save(medicoTemporal);

        HttpRequest eliminacion = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + puerto + "/clinica/v1/medico/eliminar/"
                                + medicoTemporal.getId()))
                .header("Origin", "http://localhost:4200")
                .DELETE()
                .build();
        HttpResponse<String> resultado = HttpClient.newHttpClient()
                .send(eliminacion, HttpResponse.BodyHandlers.ofString());

        org.junit.jupiter.api.Assertions.assertEquals(200, resultado.statusCode(), resultado.body());
        org.junit.jupiter.api.Assertions.assertEquals(
                "*", resultado.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
        org.junit.jupiter.api.Assertions.assertFalse(medicoRepository.existsById(medicoTemporal.getId()));
    }

    @Test
    @DisplayName("Clientes: listar, consultar, crear, actualizar y proteger eliminación")
    void operacionesCliente() throws Exception {
        verificarPreflight("/cliente/guardar", "POST");
        String nuevoCliente = """
                {"tipoDocumento":"CE","numeroDocumento":"301","nombres":"Cliente",
                 "apellidos":"Temporal","fechaNacimiento":"1995-05-10","genero":"F",
                 "telefono":"3001234567","direccion":"Calle de prueba","activo":true}
                """;
        MvcResult creado = enviar(post("/cliente/guardar"), nuevoCliente)
                .andExpect(status().isCreated())
                .andReturn();
        long clienteId = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/cliente/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/cliente/cliente-documento")
                        .param("numeroDocumento", "301")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId));
        mvc.perform(get("/cliente/" + clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Cliente"));

        String actualizacion = """
                {"id":%d,"tipoDocumento":"CE","numeroDocumento":"301","nombres":"Cliente Actualizado",
                 "apellidos":"Temporal","fechaNacimiento":"1995-05-10","genero":"F",
                 "telefono":"3001234567","direccion":"Calle nueva","activo":true}
                """.formatted(clienteId);
        enviar(post("/cliente/actualizar"), actualizacion)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId));
        org.junit.jupiter.api.Assertions.assertEquals(
                "Cliente Actualizado", clienteRepository.findById(clienteId).orElseThrow().getNombres());

        verificarPreflight("/cliente/eliminar/" + clienteId, "DELETE");
        mvc.perform(delete("/cliente/eliminar/" + clienteId).header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId));
        mvc.perform(delete("/cliente/eliminar/" + juan.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("mascotas asociadas")));
    }

    @Test
    @DisplayName("Razas: listar, crear, actualizar y proteger eliminación de razas usadas")
    void operacionesRaza() throws Exception {
        verificarPreflight("/raza/guardar", "POST");
        MvcResult creado = enviar(post("/raza/guardar"),
                "{\"nombre\":\"Beagle\",\"especie\":\"Perro\"}")
                .andExpect(status().isCreated())
                .andReturn();
        long razaId = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/raza/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        enviar(post("/raza/actualizar"),
                "{\"razaId\":" + razaId + ",\"nombre\":\"Beagle Mix\",\"especie\":\"Perro\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(razaId));
        org.junit.jupiter.api.Assertions.assertEquals(
                "Beagle Mix", razaRepository.findById((int) razaId).orElseThrow().getNombre());

        verificarPreflight("/raza/eliminar/" + razaId, "DELETE");
        mvc.perform(delete("/raza/eliminar/" + razaId).header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk());
        mvc.perform(delete("/raza/eliminar/" + max.getRaza().getRazaId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("mascotas asociadas")));
    }

    @Test
    @DisplayName("Especializaciones: crear, actualizar, listar y proteger eliminación")
    void operacionesEspecializacion() throws Exception {
        verificarPreflight("/especializacion/guardar", "POST");
        String cuerpo = """
                {"codigoEspecializacion":"OFT","nombre":"Oftalmología","descripcion":"Prueba"}
                """;
        MvcResult creado = enviar(post("/especializacion/guardar"), cuerpo)
                .andExpect(status().isCreated())
                .andReturn();
        long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/especializacion/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        enviar(post("/especializacion/actualizar"),
                "{\"id\":" + id + ",\"codigoEspecializacion\":\"OFT\",\"nombre\":\"Oftalmología clínica\","
                        + "\"descripcion\":null}")
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(
                "Oftalmología clínica", especializacionRepository.findById(id).orElseThrow().getNombre());

        verificarPreflight("/especializacion/eliminar/" + id, "DELETE");
        mvc.perform(delete("/especializacion/eliminar/" + id).header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk());
        mvc.perform(delete("/especializacion/eliminar/" + laura.getEspecializacion().getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("médicos asociados")));
    }

    @Test
    @DisplayName("Mascotas: listar, crear, actualizar y proteger eliminación de mascotas con citas")
    void operacionesMascota() throws Exception {
        verificarPreflight("/mascota/eliminar/999999", "DELETE");
        mvc.perform(get("/mascota/listar").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        String nuevaMascota = """
                {"nombreMascota":"Temporal","edad":2,"razaId":%d,"clienteId":%d}
                """.formatted(max.getRaza().getRazaId(), juan.getId());
        MvcResult creada = enviar(post("/mascota/guardar"), nuevaMascota)
                .andExpect(status().isOk())
                .andReturn();
        long mascotaId = ((Number) JsonPath.read(creada.getResponse().getContentAsString(), "$.id")).longValue();

        enviar(post("/mascota/actualizar"),
                "{\"mascotaId\":" + mascotaId + ",\"nombreMascota\":\"Temporal Actualizada\","
                        + "\"edad\":3,\"razaId\":" + max.getRaza().getRazaId()
                        + ",\"clienteId\":" + juan.getId() + "}")
                .andExpect(status().isOk());
        mvc.perform(get("/mascota/" + mascotaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreMascota").value("Temporal Actualizada"));

        mvc.perform(delete("/mascota/eliminar/" + mascotaId).header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk());
        mvc.perform(delete("/mascota/eliminar/" + max.getMascotaId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("citas o historias asociadas")));
    }

    @Test
    @DisplayName("Usuarios: listar, crear, actualizar sin cambiar contraseña y eliminar")
    void operacionesUsuario() throws Exception {
        verificarPreflight("/usuario/eliminar/999999", "DELETE");
        String usuarioNuevo = """
                {"username":"prueba","password":"clave-segura","rol":"ADMIN",
                 "email":"prueba@example.com","activo":true}
                """;
        MvcResult creado = enviar(post("/usuario/guardar"), usuarioNuevo)
                .andExpect(status().isOk())
                .andReturn();
        long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();
        String passwordHash = usuarioRepository.findById(id).orElseThrow().getPasswordHash();

        mvc.perform(get("/usuario/listar-ordenado").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("prueba"));
        enviar(post("/usuario/actualizar"),
                "{\"id\":" + id + ",\"username\":\"prueba-editada\",\"rol\":\"VETERINARIO\","
                        + "\"email\":\"prueba@example.com\",\"activo\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        Usuario actualizado = usuarioRepository.findById(id).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(passwordHash, actualizado.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertFalse(actualizado.getActivo());

        mvc.perform(delete("/usuario/eliminar/" + id).header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        org.junit.jupiter.api.Assertions.assertFalse(usuarioRepository.existsById(id));
    }

    @Test
    @DisplayName("Medicamentos: listar, crear, actualizar, validar duplicados y eliminar")
    void operacionesMedicamento() throws Exception {
        verificarPreflight("/medicamento/guardar", "POST");
        String medicamentoNuevo = """
                {"nombre":"Antibiótico de prueba","presentacion":"Tabletas 250 mg","descripcion":"Uso de prueba"}
                """;
        MvcResult creado = enviar(post("/medicamento/guardar"), medicamentoNuevo)
                .andExpect(status().isCreated())
                .andReturn();
        long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/medicamento/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Antibiótico de prueba"));
        enviar(post("/medicamento/guardar"), medicamentoNuevo)
                .andExpect(status().isConflict());
        enviar(post("/medicamento/actualizar"),
                "{\"id\":" + id + ",\"nombre\":\"Antibiótico de prueba\","
                        + "\"presentacion\":\"Jarabe 250 mg\",\"descripcion\":null}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        org.junit.jupiter.api.Assertions.assertEquals(
                "Jarabe 250 mg", medicamentoRepository.findById(id).orElseThrow().getPresentacion());

        verificarPreflight("/medicamento/eliminar/" + id, "DELETE");
        mvc.perform(delete("/medicamento/eliminar/" + id).header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertFalse(medicamentoRepository.existsById(id));
    }

    @Test
    @DisplayName("Citas: crear, validar reglas, listar con filtros/orden y cambiar estados")
    void flujoCitas() throws Exception {
        String fecha = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.HOURS).toString();
        String citaJson = """
                {"clienteId": %d, "mascotaId": %d, "medicoId": %d, "fechaCita": "%s",
                 "motivo": "Control", "estado": "PENDIENTE"}""";

        // Crear una cita válida -> 201.
        MvcResult creada = enviar(post("/cita"), citaJson.formatted(juan.getId(), max.getMascotaId(), laura.getId(), fecha))
                .andExpect(status().isCreated()).andReturn();
        long citaId = ((Number) JsonPath.read(creada.getResponse().getContentAsString(), "$.id")).longValue();

        // Mismo veterinario 10 minutos después -> 409 (veterinario no disponible).
        String fecha10 = LocalDateTime.parse(fecha).plusMinutes(10).toString();
        enviar(post("/cita"), citaJson.formatted(maria().getId(), rocky.getMascotaId(), laura.getId(), fecha10))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("no está disponible")));

        // Mascota que no pertenece al cliente -> 400.
        enviar(post("/cita"), citaJson.formatted(juan.getId(), rocky.getMascotaId(), laura.getId(), fecha10))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("no pertenece")));

        // Cliente inexistente -> 404.
        enviar(post("/cita"), citaJson.formatted(9999L, max.getMascotaId(), laura.getId(), fecha))
                .andExpect(status().isNotFound());

        // JSON mal formado -> 400 con mensaje amigable.
        enviar(post("/cita"), "{\"fechaCita\": \"no-es-fecha\"}").andExpect(status().isBadRequest());

        // Listar con filtro de texto, estado y orden por cliente.
        mvc.perform(get("/cita/listar").param("texto", "max").param("ordenarPor", "cliente")
                        .param("direccion", "asc").param("tamanio", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRegistros").value(2))
                .andExpect(jsonPath("$.tamanio").value(5));
        mvc.perform(get("/cita/listar").param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(1)));
        mvc.perform(get("/cita/listar").param("estado", "INVENTADO")).andExpect(status().isBadRequest());

        // Orden por defecto: fecha descendente (la futura primero).
        mvc.perform(get("/cita/listar"))
                .andExpect(jsonPath("$.contenido[0].citaId").value(citaId));

        // Cancelar y luego intentar editar -> 409; reactivar -> 200.
        enviar(patch("/cita/" + citaId + "/estado"), "{\"estado\": \"CANCELADA\"}").andExpect(status().isOk());
        enviar(put("/cita/" + citaId), citaJson.formatted(juan.getId(), max.getMascotaId(), laura.getId(), fecha))
                .andExpect(status().isConflict());
        enviar(patch("/cita/" + citaId + "/reactivar"), "{}")
                .andExpect(status().isOk());
        mvc.perform(get("/cita/" + citaId))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                // Las fechas viajan como texto ISO local (yyyy-MM-ddTHH:mm:ss), que es lo que espera Angular.
                .andExpect(jsonPath("$.fechaCita", startsWith(fecha.substring(0, 16))))
                .andExpect(jsonPath("$.mascota.cliente.nombres").value("Juan"));

        // Cita inexistente -> 404.
        mvc.perform(get("/cita/987654")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Historia, anotaciones y fórmulas: crear, listar y aplicar reglas")
    void flujoHistoriaYFormula() throws Exception {
        // Combos dependientes.
        mvc.perform(get("/cliente/" + juan.getId() + "/mascotas"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/cita/mascota/" + max.getMascotaId() + "/atendidas-sin-historia"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        // Crear historia de la cita atendida -> 201; repetirla -> 409.
        String historiaJson = """
                {"clienteId": %d, "mascotaId": %d, "citaId": %d, "medicoId": %d,
                 "fechaConsulta": "%s", "motivoConsulta": "Vómito", "diagnostico": "Gastroenteritis"}"""
                .formatted(juan.getId(), max.getMascotaId(), citaAtendida.getCitaId(), laura.getId(),
                        citaAtendida.getFechaCita().plusMinutes(10));
        MvcResult historia = enviar(post("/historia-medica"), historiaJson).andExpect(status().isCreated()).andReturn();
        long historiaId = ((Number) JsonPath.read(historia.getResponse().getContentAsString(), "$.id")).longValue();
        enviar(post("/historia-medica"), historiaJson).andExpect(status().isConflict());

        mvc.perform(get("/historia-medica/listar").param("texto", "juan").param("ordenarPor", "veterinario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].motivoConsulta").value("Vómito"));

        // Anotaciones.
        String anotacionJson = """
                {"historiaId": %d, "medicoId": %d, "fechaAnotacion": "%s", "descripcion": "Control",
                 "observacion": "Come bien", "diagnosticoEvolucion": "Mejoría"}"""
                .formatted(historiaId, laura.getId(), LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES));
        enviar(post("/anotacion-historia"), anotacionJson).andExpect(status().isCreated());
        mvc.perform(get("/anotacion-historia/historia/" + historiaId).param("texto", "mejor"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalRegistros").value(1));
        mvc.perform(get("/anotacion-historia/mascota/" + max.getMascotaId()).param("ordenarPor", "fecha"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalRegistros").value(1));

        // Fórmula: consulta de otra mascota -> 400; válida -> 201.
        String formulaJson = """
                {"clienteId": %d, "mascotaId": %d, "historiaId": %d, "medicoId": %d, "fechaEmision": "%s",
                 "medicamento": "Metronidazol", "dosis": 250, "unidadDosis": "mg", "frecuenciaHoras": 12,
                 "duracionDias": 5, "indicaciones": "Con comida"}""";
        enviar(post("/formula-medica"), formulaJson.formatted(maria().getId(), rocky.getMascotaId(), historiaId,
                laura.getId(), LocalDate.now())).andExpect(status().isBadRequest());
        MvcResult formula = enviar(post("/formula-medica"), formulaJson.formatted(juan.getId(), max.getMascotaId(),
                historiaId, laura.getId(), LocalDate.now())).andExpect(status().isCreated()).andReturn();
        long formulaId = ((Number) JsonPath.read(formula.getResponse().getContentAsString(), "$.id")).longValue();

        // Listado resumido ordenado por cliente.
        mvc.perform(get("/formula-medica/listar").param("ordenarPor", "cliente").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].posologia").value("250 mg cada 12 h por 5 días"))
                .andExpect(jsonPath("$.contenido[0].cliente").value("Juan Pérez"));

        // Editar, inactivar y editar inactiva -> 409.
        enviar(put("/formula-medica/" + formulaId), formulaJson.formatted(juan.getId(), max.getMascotaId(),
                historiaId, laura.getId(), LocalDate.now()).replace("\"duracionDias\": 5", "\"duracionDias\": 7"))
                .andExpect(status().isOk());
        mvc.perform(get("/formula-medica/" + formulaId)).andExpect(jsonPath("$.duracionDias").value(7));
        mvc.perform(patch("/formula-medica/" + formulaId + "/inactivar")).andExpect(status().isOk());
        enviar(put("/formula-medica/" + formulaId), formulaJson.formatted(juan.getId(), max.getMascotaId(),
                historiaId, laura.getId(), LocalDate.now())).andExpect(status().isConflict());

        // Cerrar historia y luego intentar anotar -> 409.
        mvc.perform(patch("/historia-medica/" + historiaId + "/cerrar")).andExpect(status().isOk());
        enviar(post("/anotacion-historia"), anotacionJson).andExpect(status().isConflict());
    }

    /**
     * Envía una petición con cuerpo JSON.
     *
     * @param peticion constructor de la petición (post, put, patch).
     * @param json     cuerpo de la petición.
     * @return resultado para encadenar validaciones.
     * @throws Exception si la petición falla.
     */
    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder peticion,
                                 String json) throws Exception {
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private void verificarPreflight(String ruta, String metodo) throws Exception {
        String origen = "http://localhost:4200";
        mvc.perform(options(ruta)
                        .header("Origin", origen)
                        .header("Access-Control-Request-Method", metodo)
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin",
                                org.hamcrest.Matchers.anyOf(
                                        org.hamcrest.Matchers.is("*"),
                                        org.hamcrest.Matchers.is(origen))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Methods", containsString(metodo)));
    }

    /**
     * Obtiene la clienta María (dueña de Rocky).
     *
     * @return clienta María.
     */
    private Cliente maria() {
        return rocky.getCliente();
    }

    /**
     * Crea y guarda un cliente activo.
     *
     * @param nombres   nombres del cliente.
     * @param apellidos apellidos del cliente.
     * @param documento número de documento.
     * @return cliente guardado.
     */
    private Cliente crearCliente(String nombres, String apellidos, String documento) {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento(documento);
        cliente.setNombres(nombres);
        cliente.setApellidos(apellidos);
        cliente.setActivo(true);
        return clienteRepository.save(cliente);
    }

    /**
     * Crea y guarda una mascota.
     *
     * @param nombre  nombre de la mascota.
     * @param cliente dueño.
     * @param raza    raza.
     * @return mascota guardada.
     */
    private Mascota crearMascota(String nombre, Cliente cliente, Raza raza) {
        Mascota mascota = new Mascota();
        mascota.setNombreMascota(nombre);
        mascota.setEdad(3);
        mascota.setCliente(cliente);
        mascota.setRaza(raza);
        return mascotaRepository.save(mascota);
    }
}
