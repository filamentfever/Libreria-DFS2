package com.libreriajuanin.carrito.repository;

import com.libreriajuanin.carrito.model.Carrito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByUsuario(String usuario);
}
