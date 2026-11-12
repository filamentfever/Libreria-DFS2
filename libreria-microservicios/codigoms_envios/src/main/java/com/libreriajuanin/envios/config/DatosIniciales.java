package com.libreriajuanin.envios.config;

import com.libreriajuanin.envios.model.PuntoRetiro;
import com.libreriajuanin.envios.repository.PuntoRetiroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Puntos de retiro de EJEMPLO (direcciones ficticias). */
@Configuration
public class DatosIniciales {

    @Bean
    public CommandLineRunner cargarPuntosDeRetiro(PuntoRetiroRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(new PuntoRetiro("Sucursal Centro", "Calle Ejemplo 100", "Santiago", "Lun-Vie: 9:00 - 18:00", true));
            repo.save(new PuntoRetiro("Sucursal Costanera", "Av. Ejemplo 250", "Concepción", "Lun-Sáb: 10:00 - 19:00", true));
            repo.save(new PuntoRetiro("Sucursal Puerto", "Pasaje Ejemplo 45", "Valparaíso", "Lun-Vie: 9:30 - 17:30", true));
            repo.save(new PuntoRetiro("Sucursal Norte (cerrada)", "Calle Antigua 9", "Santiago", "Sin atención", false));
        };
    }
}
