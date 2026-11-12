package com.libreriajuanin.admin.repository;

import com.libreriajuanin.admin.model.ReporteGenerado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteGeneradoRepository extends JpaRepository<ReporteGenerado, Long> {

    List<ReporteGenerado> findAllByOrderByGeneradoEnDesc();
}
