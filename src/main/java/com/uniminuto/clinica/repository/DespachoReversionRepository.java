package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.DespachoReversion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DespachoReversionRepository extends JpaRepository<DespachoReversion, Long> {
    boolean existsByDespachoId(Long despachoId);
}
