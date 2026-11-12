package com.libreriajuanin.pagos.repository;

import com.libreriajuanin.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
}
