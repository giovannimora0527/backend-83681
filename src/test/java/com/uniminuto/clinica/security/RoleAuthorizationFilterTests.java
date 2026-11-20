package com.uniminuto.clinica.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.time.Instant;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class RoleAuthorizationFilterTests {
    @Test void requestSinTokenDevuelve401() throws Exception {
        var filter = new RoleAuthorizationFilter(Encoders.BASE64.encode(Jwts.SIG.HS512.key().build().getEncoded()));
        var request = new MockHttpServletRequest(); request.setMethod("GET"); request.setServletPath("/medicamento/listar");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> fail("No debe continuar sin token"));
        assertEquals(401, response.getStatus());
    }

    @Test void despachoNoPuedeRegistrarIngresoDeInventario() throws Exception {
        var key = Jwts.SIG.HS512.key().build();
        String token = Jwts.builder().subject("10").claim("rol", "DESPACHO_MEDICO")
                .issuedAt(new Date()).expiration(Date.from(Instant.now().plusSeconds(300))).signWith(key).compact();
        var filter = new RoleAuthorizationFilter(Encoders.BASE64.encode(key.getEncoded()));
        var request = new MockHttpServletRequest(); request.setMethod("POST"); request.setServletPath("/inventario/lotes/entrada");
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> fail("El rol de despacho no puede cambiar existencias manualmente"));
        assertEquals(403, response.getStatus());
    }

    @Test void medicoPuedeConsultarDisponibilidadSinModificarla() throws Exception {
        var key=Jwts.SIG.HS512.key().build();String token=Jwts.builder().subject("10").claim("rol","MEDICO")
                .issuedAt(new Date()).expiration(Date.from(Instant.now().plusSeconds(300))).signWith(key).compact();
        var filter=new RoleAuthorizationFilter(Encoders.BASE64.encode(key.getEncoded()));
        var request=new MockHttpServletRequest();request.setMethod("GET");request.setServletPath("/cita/disponibilidad/medico");request.addHeader("Authorization","Bearer "+token);
        var response=new MockHttpServletResponse();filter.doFilter(request,response,(req,res)->((MockHttpServletResponse)res).setStatus(204));assertEquals(204,response.getStatus());
        request=new MockHttpServletRequest();request.setMethod("POST");request.setServletPath("/cita/disponibilidad/crear");request.addHeader("Authorization","Bearer "+token);
        response=new MockHttpServletResponse();filter.doFilter(request,response,(req,res)->fail("El médico no administra horarios"));assertEquals(403,response.getStatus());
    }
}
