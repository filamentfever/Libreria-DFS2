package com.libreriajuanin.notificaciones.model;

/** Ciclo de vida de una notificación en la cola. */
public enum EstadoEnvio {
    PENDIENTE,
    ENVIADO,
    FALLIDO
}
