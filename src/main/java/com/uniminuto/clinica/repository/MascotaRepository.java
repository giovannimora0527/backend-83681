package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

    List<Mascota> findAllByOrderByNombreMascotaAsc();

    List<Mascota> findAllByOrderByNombreMascotaDesc();

    Mascota findByNombreMascota(String nombreMascota);

    List<Mascota> findByClienteOrderByNombreMascotaAsc(Cliente c);

    List<Mascota> findByRazaOrderByNombreMascotaAsc(Raza raza);

    Optional<Mascota> findByMascotaIdAndClienteClienteId(Integer mascotaId, Long clienteId);

    /**
     * Lista las mascotas de un cliente ordenadas por nombre.
     *
     * @param clienteId identificador del cliente.
     * @return mascotas del cliente.
     */
    @Query("SELECT m FROM Mascota m WHERE m.cliente.clienteId = :clienteId ORDER BY m.nombreMascota ASC")
    List<Mascota> findAllByClienteIdOrderByNombreMascotaAsc(@Param("clienteId") Long clienteId);
}
