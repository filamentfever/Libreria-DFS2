package com.libreriajuanin.resenas.repository;

import com.libreriajuanin.resenas.model.Resena;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByIdProductoOrderByFechaDesc(Long idProducto);
}
