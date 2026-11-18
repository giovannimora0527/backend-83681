package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.models.CuentaMedicoRs;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Consulta y mantiene la relación entre una cuenta de acceso y su veterinario. */
@Repository
@RequiredArgsConstructor
public class MedicoUsuarioRepository {

    private static final RowMapper<CuentaMedicoRs> CUENTA = (rs, fila) ->
            new CuentaMedicoRs(rs.getLong("id"), rs.getString("username"), rs.getString("email"));
    private static final String CUENTAS_MEDICAS = """
            SELECT u.id, u.username, u.email
            FROM usuario u JOIN perfil p ON p.perfil_id = u.rol
            WHERE u.activo = 1 AND p.activo = 1
              AND TRIM(p.nombre) COLLATE utf8mb4_0900_ai_ci IN ('Medico', 'Veterinario')
            """;

    private final JdbcTemplate jdbc;

    public boolean bloquearMedico(long medicoId) {
        return !jdbc.queryForList("SELECT id FROM medico WHERE id = ? FOR UPDATE", Long.class, medicoId).isEmpty();
    }

    public Optional<CuentaMedicoRs> buscarCuenta(long medicoId) {
        return jdbc.query("""
                SELECT u.id, u.username, u.email FROM medico_usuario mu
                JOIN usuario u ON u.id = mu.usuario_id WHERE mu.medico_id = ?
                """, CUENTA, medicoId).stream().findFirst();
    }

    public List<CuentaMedicoRs> listarDisponibles() {
        return jdbc.query(CUENTAS_MEDICAS + """
                  AND NOT EXISTS (SELECT 1 FROM medico_usuario mu WHERE mu.usuario_id = u.id)
                ORDER BY u.username, u.id
                """, CUENTA);
    }

    public Optional<CuentaMedicoRs> bloquearCuentaMedica(long usuarioId) {
        return jdbc.query(CUENTAS_MEDICAS + " AND u.id = ? FOR UPDATE", CUENTA, usuarioId)
                .stream().findFirst();
    }

    public Optional<Long> bloquearVinculoPorUsuario(long usuarioId) {
        return jdbc.queryForList("SELECT medico_id FROM medico_usuario WHERE usuario_id = ? FOR UPDATE",
                Long.class, usuarioId).stream().findFirst();
    }

    public void vincular(long medicoId, long usuarioId) {
        int actualizados = jdbc.update("UPDATE medico_usuario SET usuario_id = ? WHERE medico_id = ?",
                usuarioId, medicoId);
        if (actualizados == 0) {
            jdbc.update("INSERT INTO medico_usuario (medico_id, usuario_id) VALUES (?, ?)", medicoId, usuarioId);
        }
    }

    public void desvincular(long medicoId) {
        jdbc.update("DELETE FROM medico_usuario WHERE medico_id = ?", medicoId);
    }
}
