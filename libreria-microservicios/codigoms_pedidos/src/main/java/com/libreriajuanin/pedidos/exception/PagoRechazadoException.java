package com.libreriajuanin.pedidos.exception;

/** El pago no fue aprobado (HTTP 402): el pedido NO se crea. */
public class PagoRechazadoException extends RuntimeException {

    private final boolean puedeReintentar;

    public PagoRechazadoException(String mensaje, boolean puedeReintentar) {
        super(mensaje);
        this.puedeReintentar = puedeReintentar;
    }

    public boolean isPuedeReintentar() {
        return puedeReintentar;
    }
}
