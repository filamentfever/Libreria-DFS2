package com.libreriajuanin.envios.repository;

import com.libreriajuanin.envios.model.Envio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    Optional<Envio> findByPedidoNumeroIgnoreCase(String pedidoNumero);
}
