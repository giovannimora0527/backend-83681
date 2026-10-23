package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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

    /**
     * Lista las mascotas de un cliente ordenadas por nombre. Se usa en los
     * combos dependientes Cliente → Mascota del frontend.
     *
     * @param clienteId identificador del cliente.
     * @return mascotas del cliente.
     */
    List<Mascota> findByClienteIdOrderByNombreMascotaAsc(Long clienteId);

    boolean existsByCliente_Id(Long clienteId);

    boolean existsByRaza_RazaId(Integer razaId);
}
