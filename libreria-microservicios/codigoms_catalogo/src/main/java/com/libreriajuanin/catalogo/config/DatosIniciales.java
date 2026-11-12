package com.libreriajuanin.catalogo.config;

import com.libreriajuanin.catalogo.model.Producto;
import com.libreriajuanin.catalogo.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Productos de la tienda (tomados del sitio original). Marcas, colores y fechas son datos de ejemplo. */
@Configuration
public class DatosIniciales {

    @Bean
    public CommandLineRunner cargarProductos(ProductoRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(producto("miku", "MIKU-001", "Cuaderno Miku", "5990", "Cuadernos", "Universitarios", "Torre", "Celeste,Turquesa", "/img/cuadernomiku.jpg", 12, 5,
                    "Cuaderno de edición especial con diseño exclusivo de Hatsune Miku. Tapa dura y hojas de alto gramaje ideales para dibujo o apuntes."));
            repo.save(producto("lapiz", "LAP-002", "Lapiz mina", "4990", "Lápices", "Grafito", "Faber-Castell", "Negro,Gris", "/img/lapizmina.jpg", 3, 40,
                    "Set de lápices grafito profesionales de alta calidad, perfectos para dibujo técnico, bocetos y escritura diaria."));
            repo.save(producto("resma", "RES-003", "Resma de hojas", "5990", "Papeles", "Resmas", "Proarte", "Blanco", "/img/hojascarta.jpg", 25, 60,
                    "Resma de papel tamaño carta de 75g, ultra blanca, ideal para impresiones y fotocopias sin atascos."));
            repo.save(producto("libro", "LIB-004", "Libro", "18990", "Libros", "Literatura", "Editorial Aura", "", "/img/libroaura.jpg", 8, 10,
                    "Libro de literatura contemporánea con edición ilustrada y portada de colección. Excelente opción para lectura recreativa."));
            repo.save(producto("estuche", "EST-005", "Estuche", "9990", "Accesorios", "Estuches", "Proarte", "Rosado,Negro", "/img/estuche.jpg", 7, 90,
                    "Estuche organizador de gran capacidad con múltiples compartimentos, resistente y de material duradero."));
            repo.save(producto("goma", "GOM-006", "Goma de borrar", "1190", "Accesorios", "Gomas y correctores", "Faber-Castell", "Blanco", "/img/gomadeborrar.jpg", 4, 20,
                    "Goma de borrar técnica de alta precisión, no mancha y elimina el grafito con mínimo residuo."));
        };
    }

    private Producto producto(String slug, String sku, String nombre, String precio, String categoria, String subcategoria,
                              String marca, String colores, String imagen, int stock, int diasDesdeIngreso, String descripcion) {
        Producto p = new Producto();
        p.setSlug(slug);
        p.setSku(sku);
        p.setNombre(nombre);
        p.setPrecio(new BigDecimal(precio));
        p.setCategoria(categoria);
        p.setSubcategoria(subcategoria);
        p.setMarca(marca);
        p.setColores(colores);
        p.setImagen(imagen);
        p.setStock(stock);
        p.setActivo(true);
        p.setFechaIngreso(LocalDate.now().minusDays(diasDesdeIngreso));
        p.setDescripcion(descripcion);
        return p;
    }
}
