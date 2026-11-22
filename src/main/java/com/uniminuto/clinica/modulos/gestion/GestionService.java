package com.uniminuto.clinica.modulos.gestion;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import com.uniminuto.clinica.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Gestión de clientes (propietarios) y de usuarios del sistema. */
@Service
public class GestionService {

    private final Db db;
    private final CurrentUser current;

    public GestionService(Db db, CurrentUser current) {
        this.db = db;
        this.current = current;
    }

    // ------------------------------------------------------------------ clientes

    private static final String SELECT_CLIENTE =
            "SELECT c.cliente_id AS id, c.tipo_documento, c.numero_documento, c.nombres, c.apellidos, c.fecha_nacimiento, "
                    + "c.genero, c.telefono, c.direccion, c.activo, "
                    + "(SELECT COUNT(*) FROM mascota m WHERE m.cliente_id = c.cliente_id) AS mascotas "
                    + "FROM cliente c ";

    public List<Map<String, Object>> clientes(String q, Boolean activo) {
        StringBuilder sql = new StringBuilder(SELECT_CLIENTE).append("WHERE 1=1 ");
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim() + "%";
            sql.append("AND (c.numero_documento LIKE ? OR c.nombres LIKE ? OR c.apellidos LIKE ? OR c.telefono LIKE ?) ");
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (activo != null) {
            sql.append("AND c.activo = ? ");
            args.add(activo ? 1 : 0);
        }
        sql.append("ORDER BY c.apellidos, c.nombres");
        return db.list(sql.toString(), args.toArray());
    }

    public Map<String, Object> cliente(long id) {
        return db.must("El cliente no existe.", SELECT_CLIENTE + "WHERE c.cliente_id = ?", id);
    }

    @Transactional
    public Map<String, Object> crearCliente(Map<String, Object> b) {
        Datos d = validarCliente(b, -1L);
        long id = db.insert("INSERT INTO cliente (tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, genero, "
                        + "telefono, direccion, activo) VALUES (?,?,?,?,?,?,?,?,1)",
                d.tipo, d.numero, d.nombres, d.apellidos, d.nacimiento, d.genero, d.telefono, d.direccion);
        return cliente(id);
    }

    @Transactional
    public Map<String, Object> editarCliente(long id, Map<String, Object> b) {
        cliente(id);
        Datos d = validarCliente(b, id);
        db.update("UPDATE cliente SET tipo_documento=?, numero_documento=?, nombres=?, apellidos=?, fecha_nacimiento=?, genero=?, "
                        + "telefono=?, direccion=? WHERE cliente_id=?",
                d.tipo, d.numero, d.nombres, d.apellidos, d.nacimiento, d.genero, d.telefono, d.direccion, id);
        return cliente(id);
    }

    @Transactional
    public Map<String, Object> estadoCliente(long id, boolean activo) {
        cliente(id);
        db.update("UPDATE cliente SET activo = ? WHERE cliente_id = ?", activo ? 1 : 0, id);
        return cliente(id);
    }

    private record Datos(String tipo, String numero, String nombres, String apellidos, LocalDate nacimiento,
                         String genero, String telefono, String direccion) {
    }

    private Datos validarCliente(Map<String, Object> b, long idActual) {
        String tipo = In.req(b, "tipoDocumento", "Tipo de documento").toUpperCase();
        String numero = In.req(b, "numeroDocumento", "Número de documento");
        String nombres = In.req(b, "nombres", "Nombres");
        String apellidos = In.req(b, "apellidos", "Apellidos");
        LocalDate nacimiento = In.reqDate(b, "fechaNacimiento", "Fecha de nacimiento");
        if (nacimiento.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento no puede ser futura.");
        }
        String genero = In.str(b, "genero");
        if (genero != null && !genero.isBlank() && !genero.matches("[MFO]")) {
            throw new BadRequestException("El género debe ser M, F u O.");
        }
        String telefono = In.str(b, "telefono");
        if (telefono != null && !telefono.isBlank() && !telefono.matches("[0-9+\\-() ]{7,20}")) {
            throw new BadRequestException("El teléfono solo admite números y entre 7 y 20 caracteres.");
        }
        if (numero.length() > 20 || !numero.matches("[A-Za-z0-9\\-]+")) {
            throw new BadRequestException("El número de documento es inválido (máximo 20 caracteres alfanuméricos).");
        }
        if (db.count("SELECT COUNT(*) FROM cliente WHERE numero_documento = ? AND cliente_id <> ?", numero, idActual) > 0) {
            throw new BadRequestException("Ya existe un cliente con ese número de documento.");
        }
        return new Datos(tipo, numero, nombres, apellidos, nacimiento, blankToNull(genero), blankToNull(telefono),
                blankToNull(In.str(b, "direccion")));
    }

    // ------------------------------------------------------------------ usuarios

    private static final String SELECT_USUARIO =
            "SELECT u.id, u.username, u.email, u.activo, u.fecha_creacion, u.rol AS perfil_id, p.nombre AS perfil, "
                    + "(SELECT COUNT(*) FROM medico m WHERE m.usuario_id = u.id) AS es_medico "
                    + "FROM usuario u JOIN perfil p ON p.perfil_id = u.rol ";

    public List<Map<String, Object>> perfiles() {
        return db.list("SELECT perfil_id AS id, nombre FROM perfil WHERE activo = 1 ORDER BY perfil_id");
    }

    public List<Map<String, Object>> usuarios(String q) {
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim() + "%";
            return db.list(SELECT_USUARIO + "WHERE u.username LIKE ? OR u.email LIKE ? OR p.nombre LIKE ? ORDER BY u.username",
                    like, like, like);
        }
        return db.list(SELECT_USUARIO + "ORDER BY u.username");
    }

    public Map<String, Object> usuario(long id) {
        return db.must("El usuario no existe.", SELECT_USUARIO + "WHERE u.id = ?", id);
    }

    @Transactional
    public Map<String, Object> crearUsuario(Map<String, Object> b) {
        String username = In.req(b, "username", "Usuario");
        String email = In.req(b, "email", "Correo electrónico");
        long perfilId = In.reqLng(b, "perfilId", "Perfil");
        String password = In.req(b, "password", "Contraseña");
        validarUsuario(username, email, perfilId, password, -1L);
        long id = db.insert("INSERT INTO usuario (username, password_hash, rol, activo, email, fecha_creacion) "
                + "VALUES (?,?,?,1,?,NOW())", username, PasswordUtil.generarMD5(password), perfilId, email);
        return usuario(id);
    }

    @Transactional
    public Map<String, Object> editarUsuario(long id, Map<String, Object> b) {
        usuario(id);
        String username = In.req(b, "username", "Usuario");
        String email = In.req(b, "email", "Correo electrónico");
        long perfilId = In.reqLng(b, "perfilId", "Perfil");
        String password = In.str(b, "password");
        validarUsuario(username, email, perfilId, password, id);
        if (id == current.get().id() && perfilId != In.toLong(usuario(id).get("perfilId"))) {
            throw new BadRequestException("No puede cambiar su propio perfil.");
        }
        db.update("UPDATE usuario SET username=?, email=?, rol=? WHERE id=?", username, email, perfilId, id);
        if (password != null && !password.isBlank()) {
            db.update("UPDATE usuario SET password_hash=? WHERE id=?", PasswordUtil.generarMD5(password), id);
        }
        return usuario(id);
    }

    @Transactional
    public Map<String, Object> estadoUsuario(long id, boolean activo) {
        usuario(id);
        if (!activo && id == current.get().id()) {
            throw new BadRequestException("No puede inactivar su propio usuario.");
        }
        db.update("UPDATE usuario SET activo = ? WHERE id = ?", activo ? 1 : 0, id);
        if (!activo) {
            db.update("DELETE FROM session WHERE user_id = ?", id);
        }
        return usuario(id);
    }

    private void validarUsuario(String username, String email, long perfilId, String password, long idActual) {
        if (!username.matches("[A-Za-z0-9._-]{4,50}")) {
            throw new BadRequestException("El usuario debe tener entre 4 y 50 caracteres (letras, números, punto, guion).");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || email.length() > 255) {
            throw new BadRequestException("El correo electrónico no es válido.");
        }
        if (password != null && !password.isBlank() && password.length() < 8) {
            throw new BadRequestException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (db.count("SELECT COUNT(*) FROM perfil WHERE perfil_id = ? AND activo = 1", perfilId) == 0) {
            throw new BadRequestException("El perfil no existe o está inactivo.");
        }
        if (db.count("SELECT COUNT(*) FROM usuario WHERE username = ? AND id <> ?", username, idActual) > 0) {
            throw new BadRequestException("El nombre de usuario ya existe.");
        }
        if (db.count("SELECT COUNT(*) FROM usuario WHERE email = ? AND id <> ?", email, idActual) > 0) {
            throw new BadRequestException("El correo electrónico ya está registrado.");
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
