package com.uniminuto.clinica.config;

import com.uniminuto.clinica.entity.Perfil;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.PerfilRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapAdminInitializer.class);
    private static final String ADMIN_ROLE = "ADMINISTRADOR";

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public BootstrapAdminInitializer(
            PerfilRepository perfilRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${BOOTSTRAP_ADMIN_USERNAME:}") String username,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            logger.info("No se creó administrador inicial; configure BOOTSTRAP_ADMIN_USERNAME, "
                    + "BOOTSTRAP_ADMIN_EMAIL y BOOTSTRAP_ADMIN_PASSWORD si la base es nueva.");
            return;
        }

        Perfil adminProfile = perfilRepository.findByNombreIgnoreCase(ADMIN_ROLE)
                .orElseThrow(() -> new IllegalStateException("No existe el perfil ADMINISTRADOR."));
        if (usuarioRepository.existsByPerfil_PerfilId(adminProfile.getPerfilId())) {
            return;
        }
        if (usuarioRepository.existsByUsername(username) || usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("Los datos de bootstrap coinciden con un usuario de otro perfil.");
        }

        Usuario admin = new Usuario();
        admin.setUsername(username.trim());
        admin.setEmail(email.trim().toLowerCase(Locale.ROOT));
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setPerfil(adminProfile);
        admin.setActivo(true);
        admin.setFechaCreacion(LocalDateTime.now());
        usuarioRepository.save(admin);
        logger.info("Se creó el administrador inicial configurado por entorno.");
    }
}
