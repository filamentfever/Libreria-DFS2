package com.libreriajuanin.pedidos.repository;

import com.libreriajuanin.pedidos.model.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumeroIgnoreCase(String numero);

    List<Pedido> findByUsuarioOrderByFechaDescIdDesc(String usuario);
}
