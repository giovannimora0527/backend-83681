package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Menu;

import java.util.List;

/** Define las operaciones disponibles para gestionar una opción de navegación. */
public interface MenuService {
    /** Obtiene las opciones de menú que corresponden al perfil. */
    List<Menu> obtenerListaMenus(Long usuarioId);
}
