package com.libreriajuanin.catalogo;

import com.libreriajuanin.catalogo.dto.ProductoRequestDTO;
import com.libreriajuanin.catalogo.model.Producto;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Producto unProducto(Long id, String nombre, String categoria, String subcategoria,
                                      String marca, String precio, int stock, String colores, int diasDesdeIngreso) {
        Producto p = new Producto();
        p.setId(id);
        p.setSlug(nombre.toLowerCase().replace(' ', '-'));
        p.setSku("SKU-" + id);
        p.setNombre(nombre);
        p.setDescripcion("Descripción de " + nombre);
        p.setPrecio(new BigDecimal(precio));
        p.setCategoria(categoria);
        p.setSubcategoria(subcategoria);
        p.setMarca(marca);
        p.setColores(colores);
        p.setImagen("/img/" + id + ".jpg");
        p.setStock(stock);
        p.setActivo(true);
        p.setFechaIngreso(LocalDate.now().minusDays(diasDesdeIngreso));
        return p;
    }

    /** Producto activo con 10 unidades, ingresado hace 30 días. */
    public static Producto unProducto() {
        return unProducto(1L, "Cuaderno Miku", "Cuadernos", "Universitarios", "Torre", "19990", 10, "Celeste,Turquesa", 30);
    }

    public static ProductoRequestDTO unRequest() {
        ProductoRequestDTO d = new ProductoRequestDTO();
        d.setSku("NEW-100");
        d.setNombre("Destacador Neón");
        d.setPrecio(new BigDecimal("1990"));
        d.setCategoria("Lápices");
        d.setSubcategoria("Destacadores");
        d.setMarca("Torre");
        d.setStock(20);
        return d;
    }
}
