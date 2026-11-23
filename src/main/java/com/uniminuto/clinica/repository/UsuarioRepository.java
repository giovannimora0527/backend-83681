package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Metodo que lista los usuarios organizados alfabeticamente.
     * @param ascendente bandera para odenar ascendente o descendente.
     * @return Lista<Usuario> </>.
     */
    @EntityGraph(attributePaths = "perfil")
    List<Usuario> findAllByOrderByUsernameAsc();

    /**
     * Metodo que verifica si un usuario existe por su nombre de usuario.
     * @param username nombre de usuario.
     * @return true si existe, false si no existe.
     */
    Boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Metodo que verifica si un usuario existe por su correo electronico.
     * @param email correo electronico.
     * @return true si existe, false si no existe.
     */
    Boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPerfil_PerfilId(Integer perfilId);

    long countByPerfil_PerfilIdAndActivoTrue(Integer perfilId);

    /**
     * Metodo que busca un usuario por su nombre de usuario.
     * @param username nombre de usuario.
     * @return Optional<Usuario> </>.
     */
    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findFirstByEmailIgnoreCase(String email);

}
