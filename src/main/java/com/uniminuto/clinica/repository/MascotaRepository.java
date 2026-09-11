package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    /**
     * Busca una mascota por su nombre.
     * @param nombre Nombre de la mascota a buscar.
     * @return La mascota encontrada o null si no existe.
     */
    Mascota findByNombreMascota(String nombre);

    /**
    * Busca todas las mascotas ordenadas por nombre de forma ascendente.
    *
    * @return Lista de mascotas ordenadas por nombre ascendente.
    */
    List<Mascota> findAllByOrderByNombreMascotaAsc();

    /**
     * Busca todas las mascotas ordenadas por nombre de forma descendente.
     * @return Lista de mascotas ordenadas por nombre descendente.
     */
    List<Mascota> findAllByOrderByNombreMascotaDesc();
}
