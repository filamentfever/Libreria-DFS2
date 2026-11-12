package com.libreriajuanin.pagos.repository;

import com.libreriajuanin.pagos.model.Cupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuponRepository extends JpaRepository<Cupon, Long> {

    Optional<Cupon> findByCodigoIgnoreCase(String codigo);
}
