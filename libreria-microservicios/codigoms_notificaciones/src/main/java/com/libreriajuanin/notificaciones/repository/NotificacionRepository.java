package com.libreriajuanin.notificaciones.repository;

import com.libreriajuanin.notificaciones.model.EstadoEnvio;
import com.libreriajuanin.notificaciones.model.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByEstadoEnvio(EstadoEnvio estadoEnvio);
}
