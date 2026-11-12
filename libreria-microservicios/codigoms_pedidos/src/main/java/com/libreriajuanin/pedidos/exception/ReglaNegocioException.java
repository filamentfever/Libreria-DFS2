package com.libreriajuanin.pedidos.exception;

/** Se viola una regla de negocio (HTTP 400/409). */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
