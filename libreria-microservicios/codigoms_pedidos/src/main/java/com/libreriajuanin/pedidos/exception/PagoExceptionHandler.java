package com.libreriajuanin.pedidos.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PagoExceptionHandler {

    @ExceptionHandler(PagoRechazadoException.class)
    public ResponseEntity<Map<String, Object>> pagoRechazado(PagoRechazadoException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", HttpStatus.PAYMENT_REQUIRED.value());
        body.put("mensaje", ex.getMessage());
        body.put("puedeReintentar", ex.isPuedeReintentar());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
    }
}
