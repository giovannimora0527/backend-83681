package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByUsernameAsc();
}