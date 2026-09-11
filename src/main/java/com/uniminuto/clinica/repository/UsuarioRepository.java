package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email (usado para validar que sea unico).
     * @param email email a buscar.
     * @return posible usuario encontrado.
     */
    Optional<Usuario> findByEmail(String email);
}
