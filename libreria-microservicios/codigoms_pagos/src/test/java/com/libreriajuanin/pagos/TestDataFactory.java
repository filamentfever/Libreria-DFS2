package com.libreriajuanin.pagos;

import com.libreriajuanin.pagos.dto.PagoTarjetaRequestDTO;
import java.math.BigDecimal;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    /** Tarjeta de prueba aprobada (4111 1111 1111 1111), vence en 12/99. */
    public static PagoTarjetaRequestDTO unaTarjeta() {
        PagoTarjetaRequestDTO t = new PagoTarjetaRequestDTO();
        t.setTitular("Ana Pérez");
        t.setNumero("4111 1111 1111 1111");
        t.setVencimiento("12/99");
        t.setCvv("123");
        t.setMonto(new BigDecimal("40000"));
        t.setReferencia("ORD-TEST");
        return t;
    }
}
