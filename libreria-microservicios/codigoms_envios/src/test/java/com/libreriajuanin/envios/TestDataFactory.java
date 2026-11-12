package com.libreriajuanin.envios;

import com.libreriajuanin.envios.dto.EnvioRequestDTO;
import com.libreriajuanin.envios.model.EstadoDespacho;
import com.libreriajuanin.envios.model.Envio;
import com.libreriajuanin.envios.model.PuntoRetiro;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static EnvioRequestDTO unaSolicitud() {
        EnvioRequestDTO d = new EnvioRequestDTO();
        d.setPedidoNumero("ORD-1001");
        d.setDestinatario("Ana Pérez");
        d.setDireccion("Calle Uno 123");
        d.setComuna("Santiago");
        d.setNotas("dejar en conserjería");
        return d;
    }

    public static Envio unEnvio(EstadoDespacho estado) {
        Envio e = new Envio();
        e.setId(1L);
        e.setPedidoNumero("ORD-1001");
        e.setDestinatario("Ana Pérez");
        e.setDireccion("Calle Uno 123");
        e.setComuna("Santiago");
        e.setCosto(new BigDecimal("5900"));
        e.setDiasMin(2);
        e.setDiasMax(4);
        e.setEstadoDespacho(estado);
        e.setFechaCreacion(LocalDateTime.now());
        return e;
    }

    public static PuntoRetiro unPunto(Long id, String ciudad, boolean activo) {
        PuntoRetiro p = new PuntoRetiro("Sucursal " + ciudad, "Calle Ejemplo 100", ciudad, "Lun-Vie: 9:00 - 18:00", activo);
        p.setId(id);
        return p;
    }
}
