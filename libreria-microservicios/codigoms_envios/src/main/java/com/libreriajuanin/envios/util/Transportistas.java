package com.libreriajuanin.envios.util;

import java.util.Map;
import java.util.Optional;

/** Transportistas conocidos y su URL de seguimiento (URLs referenciales: ajustar a las reales). */
public final class Transportistas {

    private static final Map<String, String[]> TRANSPORTISTAS = Map.of(
            "starken", new String[] {"Starken", "https://www.starken.cl/seguimiento?codigo="},
            "chilexpress", new String[] {"Chilexpress", "https://www.chilexpress.cl/seguimiento?codigo="},
            "bluexpress", new String[] {"BlueExpress", "https://www.bluex.cl/seguimiento?codigo="});

    private Transportistas() {
    }

    /** Devuelve {nombre, urlBase} si el transportista existe (ignora mayúsculas). */
    public static Optional<String[]> buscar(String nombre) {
        if (nombre == null) return Optional.empty();
        return Optional.ofNullable(TRANSPORTISTAS.get(nombre.trim().toLowerCase()));
    }
}
