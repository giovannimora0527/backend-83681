package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de la entidad Usuario.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Metodo que lista los usuarios organizados alfabeticamente.
     * @param ascendente bandera para odenar ascendente o descendente.
     * @return Lista<Usuario> </>.
     */
    List<Usuario> findAllByOrderByUsernameAsc();

    /**
     * Metodo que verifica si un usuario existe por su nombre de usuario.
     * @param username nombre de usuario.
     * @return true si existe, false si no existe.
     */
    Boolean existsByUsername(String username);

    /**
     * Metodo que verifica si un usuario existe por su correo electronico.
     * @param email correo electronico.
     * @return true si existe, false si no existe.
     */
    Boolean existsByEmail(String email);

}
