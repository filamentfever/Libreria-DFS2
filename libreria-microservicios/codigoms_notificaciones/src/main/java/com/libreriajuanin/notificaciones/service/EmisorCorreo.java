package com.libreriajuanin.notificaciones.service;

/** Puerto de salida: quien realmente entrega el correo (simulado o SMTP). Lanza RuntimeException si falla. */
public interface EmisorCorreo {

    void enviar(String destinatario, String asunto, String mensaje);
}
