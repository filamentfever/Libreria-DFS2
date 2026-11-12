package com.libreriajuanin.catalogo.repository;

import com.libreriajuanin.catalogo.model.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    Optional<Producto> findBySlugAndActivoTrue(String slug);

    Optional<Producto> findBySkuIgnoreCase(String sku);

    boolean existsBySlug(String slug);
}
