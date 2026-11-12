package com.libreriajuanin.envios.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte las excepciones del servicio en respuestas HTTP con un cuerpo JSON uniforme. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException ex) {
        return cuerpo(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> regla(ReglaNegocioException ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<Map<String, Object>> validacion(ValidacionException ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, "Hay datos inválidos.", ex.getErrores());
    }

    private ResponseEntity<Map<String, Object>> cuerpo(HttpStatus estado, String mensaje, Map<String, String> errores) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", estado.value());
        body.put("mensaje", mensaje);
        if (errores != null) body.put("errores", errores);
        return ResponseEntity.status(estado).body(body);
    }
}
