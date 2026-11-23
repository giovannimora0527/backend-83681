package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.OtpToken;
import com.uniminuto.clinica.entity.Session;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RecuperarPasswordRq;
import com.uniminuto.clinica.models.RestablecerPasswordRq;
import com.uniminuto.clinica.repository.OtpTokenRepository;
import com.uniminuto.clinica.repository.SessionRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.TokenHashUtil;
import com.uniminuto.clinica.service.AuthService;
import com.uniminuto.clinica.service.EmailService;
import com.uniminuto.clinica.util.PasswordUtil;
import com.uniminuto.clinica.util.Utilidades;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String FROM = "clinicaveterinariauniminutosis@gmail.com";

    private static final Integer OTP_TOKEN_EXPIRATION_MINUTES = 15; // Token expiration time in minutes

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${OTP_CONSOLE_MODE:false}")
    private boolean otpConsoleMode;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Utilidades utilidades;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthRs login(AuthRq authRq) throws BadRequestException {
        this.validateAuthRq(authRq);
        Optional<Usuario> optUser = this.usuarioRepository.findByUsername(authRq.getUsername());
        if (optUser.isEmpty()) {
            throw new BadRequestException("Usuario o Password incorrecto. Por favor intente de nuevo.");
        }

        Usuario user = optUser.get();
        if (!Boolean.TRUE.equals(user.getActivo()) || !validarPasswordYActualizarHash(user, authRq.getPassword())) {
            throw new BadRequestException("Usuario o Password incorrecto. Por favor intente de nuevo.");
        }

        String tokenOtp = Utilidades.generarTokenOtp();
        if (otpConsoleMode) {
            logger.warn("OTP de acceso local para {}: {} (modo solo para desarrollo local)",
                    user.getUsername(), tokenOtp);
        } else {
            String bodyHtml = this.generarHtmlBodyCorreo(user.getUsername(), tokenOtp);
            this.emailService.enviarCorreo(user.getEmail(), "Token OTP", bodyHtml, FROM);
        }
        this.guardarOtp(user, tokenOtp);

        AuthRs rta = new AuthRs();
        rta.setUsuarioId(user.getId());
        rta.setLogin(Boolean.TRUE);
        rta.setUsername(user.getUsername());
        return rta;
    }

    @Override
    @Transactional
    public MiRespuestaRS recuperarPassword(RecuperarPasswordRq request) throws BadRequestException {
        String email = request == null || request.getEmail() == null ? "" : request.getEmail().trim();
        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BadRequestException("Ingresa un correo electrónico válido.");
        }

        Optional<Usuario> optUser = this.usuarioRepository.findFirstByEmailIgnoreCase(email);
        if (optUser.isPresent() && Boolean.TRUE.equals(optUser.get().getActivo())) {
            Usuario user = optUser.get();
            String codigo = Utilidades.generarTokenOtp();
            this.guardarOtp(user, codigo);
            if (otpConsoleMode) {
                logger.warn("Código de recuperación local para {}: {} (modo solo para desarrollo local)",
                        user.getUsername(), codigo);
            } else {
                String body = "Estimado usuario " + user.getUsername() + ",\n\n"
                        + "Su código para restablecer la contraseña es: " + codigo + "\n\n"
                        + "El código vence en " + OTP_TOKEN_EXPIRATION_MINUTES + " minutos. "
                        + "Si usted no lo solicitó, ignore este mensaje.\n\n"
                        + "Saludos,\nEquipo de Soporte";
                this.emailService.enviarCorreo(user.getEmail(), "Recuperación de contraseña", body, FROM);
            }
        }

        // Misma respuesta exista o no el correo, para no revelar qué cuentas están registradas.
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Si el correo está registrado, recibirás un código para restablecer tu contraseña.");
        return rta;
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public MiRespuestaRS restablecerPassword(RestablecerPasswordRq request) throws BadRequestException {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                || request.getToken() == null || request.getToken().isBlank()
                || request.getNuevaPassword() == null) {
            throw new BadRequestException("Correo, código y nueva contraseña son obligatorios.");
        }
        String nueva = request.getNuevaPassword();
        if (nueva.length() < 6 || nueva.getBytes(StandardCharsets.UTF_8).length > 72 || nueva.isBlank()) {
            throw new BadRequestException("La nueva contraseña debe tener entre 6 y 72 caracteres.");
        }

        Usuario user = this.usuarioRepository.findFirstByEmailIgnoreCase(request.getEmail().trim())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new BadRequestException("El código es inválido o ha expirado. Solicite uno nuevo."));

        TokenRq tokenRq = new TokenRq();
        tokenRq.setUsuarioId(user.getId());
        tokenRq.setToken(request.getToken().trim());
        this.validateOtpToken(tokenRq); // valida vigencia, uso único y máximo 3 intentos

        user.setPasswordHash(passwordEncoder.encode(nueva));
        this.usuarioRepository.save(user);
        this.sessionRepository.deleteByUserId(user.getId());

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("La contraseña fue actualizada. Ya puedes iniciar sesión.");
        return rta;
    }

    /**
     * Reemplaza el código vigente del usuario (login u recuperación) por uno nuevo.
     */
    private void guardarOtp(Usuario user, String codigo) {
        this.otpTokenRepository.findByUsuarioId(user.getId())
                .ifPresent(this.otpTokenRepository::delete);
        this.otpTokenRepository.flush();
        OtpToken token = new OtpToken();
        token.setUsuarioId(user.getId());
        token.setCodigo(TokenHashUtil.sha256Hex(codigo));
        token.setUsado(Boolean.FALSE);
        token.setIntentos(0);
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(OTP_TOKEN_EXPIRATION_MINUTES));
        this.otpTokenRepository.save(token);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public LoginRs validateToken(TokenRq tokenRq) throws BadRequestException {
        OtpToken otpToken = this.validateOtpToken(tokenRq);
        LoginRs loginRs = new LoginRs();
        //  Genero el JWT Token para el usuario
        String jwtToken = utilidades.crearJwtToken(otpToken, jwtSecret);
        loginRs.setToken(jwtToken);
        loginRs.setLogueoExitoso(Boolean.TRUE);
        // Guardo el objeto de la session
        Session session = new Session();
        session.setUserId(otpToken.getUsuarioId());
        session.setTokenHash(TokenHashUtil.sha256Hex(jwtToken));
        session.setFechaIniSesion(LocalDateTime.now());
        session.setFechaExpiracion(LocalDateTime.now().plusMinutes(60));
        this.sessionRepository.save(session);
        return loginRs;
    }

    @Override
    public CloseSessionRs cerrarSesion(CloseSession closeSession) throws BadRequestException {
        if (closeSession == null || closeSession.getToken() == null || closeSession.getToken().isBlank()) {
            throw new BadRequestException("El token de sesión es obligatorio.");
        }

        String token = closeSession.getToken().trim();
        String tokenHash = TokenHashUtil.sha256Hex(token);
        if (this.sessionRepository.findByTokenHash(tokenHash).isEmpty()) {
            throw new BadRequestException("La sesión ya no está activa.");
        }
        this.sessionRepository.deleteByTokenHash(tokenHash);

        CloseSessionRs response = new CloseSessionRs();
        response.setSuccess(true);
        response.setMessage("Sesión cerrada correctamente.");
        return response;
    }

    private String generarHtmlBodyCorreo(String username, String otpToken) {
        return "Estimado usuario " + username + ",\n\n" +
                "Su token OTP es: " + otpToken + "\n\n" +
                "Por favor, utilice este token para completar su autenticación.\n\n" +
                "Saludos,\n" +
                "Equipo de Soporte";

    }

    private OtpToken validateOtpToken(TokenRq tokenRq) throws BadRequestException {
        if (tokenRq == null || tokenRq.getUsuarioId() == null || tokenRq.getToken() == null) {
            throw new BadRequestException("UsuarioId o Token no puede ser nulo");
        }
        if (tokenRq.getToken().isBlank()) {
            throw new BadRequestException("Token OTP no puede estar vacío");
        }
        Optional<OtpToken> optToken = this.otpTokenRepository
                .findByUsuarioId(tokenRq.getUsuarioId());
        if (optToken.isEmpty()) {
            throw new BadRequestException("Se ha presentado un error con el token OTP. " +
                    "Contacte al administrador del sistema.");
        }
        OtpToken otpToken = optToken.get();
        if (Boolean.TRUE.equals(otpToken.getUsado())) {
            throw new BadRequestException("El token OTP ya fue utilizado. Solicite uno nuevo.");
        }
        if (otpToken.getIntentos() >= 3) {
            throw new BadRequestException("Se ha excedido el número máximo de intentos para validar el token OTP.");
        }
        if (otpToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token OTP ha expirado. Por favor, solicite un nuevo token.");
        }
        String codigoHash = TokenHashUtil.sha256Hex(tokenRq.getToken().trim().toUpperCase(Locale.ROOT));
        if (!MessageDigest.isEqual(
                otpToken.getCodigo().getBytes(StandardCharsets.US_ASCII),
                codigoHash.getBytes(StandardCharsets.US_ASCII))) {
            otpToken.setIntentos(otpToken.getIntentos() + 1);
            this.otpTokenRepository.save(otpToken);
            throw new BadRequestException("Token OTP incorrecto. Por favor, intente de nuevo.");
        }
        otpToken.setUsado(Boolean.TRUE);
        this.otpTokenRepository.save(otpToken);
        return otpToken;
    }

    private void validateAuthRq(AuthRq authRq) throws BadRequestException {
        if (authRq == null || authRq.getUsername() == null || authRq.getPassword() == null) {
            throw new BadRequestException("Usuario o password no puede ser nulo");
        }

        if (authRq.getUsername().isBlank() || authRq.getPassword().isEmpty()) {
            throw new BadRequestException("Usuario o password no puede estar vacío");
        }
    }

    private boolean validarPasswordYActualizarHash(Usuario usuario, String passwordIngresado) {
        String storedHash = usuario.getPasswordHash();
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")
                || storedHash.startsWith("$2y$")) {
            return passwordEncoder.matches(passwordIngresado, storedHash);
        }

        byte[] submittedLegacyHash = PasswordUtil.generarMD5(passwordIngresado)
                .getBytes(StandardCharsets.US_ASCII);
        boolean matches = MessageDigest.isEqual(
                submittedLegacyHash,
                storedHash.getBytes(StandardCharsets.US_ASCII));
        if (matches) {
            usuario.setPasswordHash(passwordEncoder.encode(passwordIngresado));
            usuarioRepository.save(usuario);
        }
        return matches;
    }
}
