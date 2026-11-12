package com.libreriajuanin.carrito.repository;

import com.libreriajuanin.carrito.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
}
