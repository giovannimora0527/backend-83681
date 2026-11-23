package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

<<<<<<< HEAD
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mascota m where m.mascotaId = :id")
    Optional<Mascota> findByIdForUpdate(@Param("id") Integer id);

=======
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    List<Mascota> findAllByOrderByNombreMascotaAsc();

    List<Mascota> findAllByOrderByNombreMascotaDesc();

    Mascota findByNombreMascota(String nombreMascota);

    List<Mascota> findByClienteOrderByNombreMascotaAsc(Cliente c);

    List<Mascota> findByRazaOrderByNombreMascotaAsc(Raza raza);
<<<<<<< HEAD
=======

    Optional<Mascota> findByMascotaIdAndClienteClienteId(Integer mascotaId, Long clienteId);

    /**
     * Lista las mascotas de un cliente ordenadas por nombre.
     *
     * @param clienteId identificador del cliente.
     * @return mascotas del cliente.
     */
    @Query("SELECT m FROM Mascota m WHERE m.cliente.clienteId = :clienteId ORDER BY m.nombreMascota ASC")
    List<Mascota> findAllByClienteIdOrderByNombreMascotaAsc(@Param("clienteId") Long clienteId);
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
