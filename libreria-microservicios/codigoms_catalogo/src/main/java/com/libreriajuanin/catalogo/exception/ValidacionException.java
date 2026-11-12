package com.libreriajuanin.catalogo.exception;

import java.util.Map;

/** Datos inválidos: lleva un mensaje por campo (HTTP 400). */
public class ValidacionException extends RuntimeException {

    private final transient Map<String, String> errores;

    public ValidacionException(Map<String, String> errores) {
        super("Hay datos inválidos: " + errores);
        this.errores = errores;
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
