package com.libreriajuanin.notificaciones.service;

import java.util.logging.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Modo por defecto: no envía nada, solo deja constancia en el log. */
@Component
@ConditionalOnProperty(name = "app.correo.modo", havingValue = "simulado", matchIfMissing = true)
public class EmisorCorreoSimulado implements EmisorCorreo {

    private static final Logger LOG = Logger.getLogger(EmisorCorreoSimulado.class.getName());

    @Override
    public void enviar(String destinatario, String asunto, String mensaje) {
        LOG.info("[correo simulado] para=" + destinatario + " asunto=" + asunto);
    }
}
