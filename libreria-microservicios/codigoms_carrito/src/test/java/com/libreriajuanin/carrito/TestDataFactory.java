package com.libreriajuanin.carrito;

import com.libreriajuanin.carrito.dto.ProductoDTO;
import com.libreriajuanin.carrito.model.Carrito;
import java.math.BigDecimal;

/** Fábrica de datos de prueba: crea objetos sin pasar por JPA ni por HTTP. */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Carrito unCarrito() {
        Carrito carrito = new Carrito("juan");
        carrito.setId(1L);
        return carrito;
    }

    /** Producto con stock de sobra (10 unidades). */
    public static ProductoDTO unProductoDTO() {
        ProductoDTO p = new ProductoDTO();
        p.setId(10L);
        p.setNombre("Cuaderno Miku");
        p.setPrecio(new BigDecimal("19990.00"));
        p.setStock(10);
        p.setActivo(true);
        p.setImagen("/img/cuadernomiku.jpg");
        return p;
    }

    /** Otro producto, con poco stock (3 unidades). */
    public static ProductoDTO otroProductoDTO() {
        ProductoDTO p = new ProductoDTO();
        p.setId(11L);
        p.setNombre("Lapiz mina");
        p.setPrecio(new BigDecimal("12500.00"));
        p.setStock(3);
        p.setActivo(true);
        p.setImagen("/img/lapizmina.jpg");
        return p;
    }
}
