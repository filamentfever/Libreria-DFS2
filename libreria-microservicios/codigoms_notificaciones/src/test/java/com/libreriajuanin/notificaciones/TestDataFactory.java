package com.libreriajuanin.notificaciones;

import com.libreriajuanin.notificaciones.model.EstadoEnvio;
import com.libreriajuanin.notificaciones.model.Notificacion;
import com.libreriajuanin.notificaciones.model.TipoEvento;
import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Notificacion unaNotificacion(Long id, EstadoEnvio estado, int intentos) {
        Notificacion n = new Notificacion();
        n.setId(id);
        n.setTipoEvento(TipoEvento.ALERTA_GENERAL);
        n.setDestinatario("ana@mail.cl");
        n.setAsunto("Alerta");
        n.setMensaje("Stock crítico");
        n.setEstadoEnvio(estado);
        n.setIntentos(intentos);
        n.setFechaCreacion(LocalDateTime.now());
        return n;
    }
}
