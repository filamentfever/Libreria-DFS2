package com.libreriajuanin.pedidos.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CalculadoraEnvioTest {

    @Test
    void cotizar_ignoraMayusculasYTildes() {
        assertThat(CalculadoraEnvio.cotizar("concepcion")).isPresent();
        assertThat(CalculadoraEnvio.cotizar("VALPARAÍSO")).isPresent();
    }

    @Test
    void cotizar_comunaDesconocidaOVacia_noTieneCobertura() {
        assertThat(CalculadoraEnvio.cotizar("Atlantis")).isEmpty();
        assertThat(CalculadoraEnvio.cotizar("")).isEmpty();
        assertThat(CalculadoraEnvio.cotizar(null)).isEmpty();
    }

    @Test
    void cotizar_incluyeElMensajeDeEntregaEstimada() {
        assertThat(CalculadoraEnvio.cotizar("Concepción").get().getMensajeEntrega())
                .isEqualTo("Entrega estimada entre 1 y 2 días hábiles");
    }
}
