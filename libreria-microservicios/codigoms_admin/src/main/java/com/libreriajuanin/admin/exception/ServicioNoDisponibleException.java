package com.libreriajuanin.admin.exception;

/** Un microservicio del que depende el reporte no respondió (HTTP 503). */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
