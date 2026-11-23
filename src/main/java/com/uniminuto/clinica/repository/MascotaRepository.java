package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mascota m where m.mascotaId = :id")
    Optional<Mascota> findByIdForUpdate(@Param("id") Integer id);

    List<Mascota> findAllByOrderByNombreMascotaAsc();

    List<Mascota> findAllByOrderByNombreMascotaDesc();

    Mascota findByNombreMascota(String nombreMascota);

    List<Mascota> findByClienteOrderByNombreMascotaAsc(Cliente c);

    List<Mascota> findByRazaOrderByNombreMascotaAsc(Raza raza);
}
