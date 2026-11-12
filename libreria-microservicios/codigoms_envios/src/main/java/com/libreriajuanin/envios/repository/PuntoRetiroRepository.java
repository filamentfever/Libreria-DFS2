package com.libreriajuanin.envios.repository;

import com.libreriajuanin.envios.model.PuntoRetiro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuntoRetiroRepository extends JpaRepository<PuntoRetiro, Long> {

    List<PuntoRetiro> findByActivoTrue();
}
