package com.libreriajuanin.pagos.config;

import com.libreriajuanin.pagos.model.Cupon;
import com.libreriajuanin.pagos.repository.CuponRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosIniciales {

    /** Cupón de ejemplo (HU-57): VUELTAACLASES = 15% de descuento. */
    @Bean
    public CommandLineRunner cargarCupones(CuponRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Cupon("VUELTAACLASES", 15, true, null));
            }
        };
    }
}
