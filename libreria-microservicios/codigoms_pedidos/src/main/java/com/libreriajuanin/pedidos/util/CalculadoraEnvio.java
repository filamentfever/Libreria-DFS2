package com.libreriajuanin.pedidos.util;

import com.libreriajuanin.pedidos.dto.CotizacionEnvioDTO;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Tarifas y plazos de despacho por comuna (HU-39, HU-40, HU-44). Valores de EJEMPLO. */
public final class CalculadoraEnvio {

    private record Tarifa(String comuna, int costo, int diasMin, int diasMax) {
    }

    private static final Map<String, Tarifa> TARIFAS = new LinkedHashMap<>();

    static {
        agregar("Concepción", 3500, 1, 2);
        agregar("Talcahuano", 3900, 1, 3);
        agregar("San Pedro de la Paz", 3900, 1, 3);
        agregar("Chiguayante", 3900, 1, 3);
        agregar("Hualpén", 3900, 1, 3);
        agregar("Santiago", 5900, 2, 4);
        agregar("Providencia", 5900, 2, 4);
        agregar("Las Condes", 5900, 2, 4);
        agregar("Valparaíso", 6500, 3, 5);
        agregar("Temuco", 6900, 3, 5);
    }

    private CalculadoraEnvio() {
    }

    private static void agregar(String comuna, int costo, int diasMin, int diasMax) {
        TARIFAS.put(normalizar(comuna), new Tarifa(comuna, costo, diasMin, diasMax));
    }

    /** Ignora mayúsculas y tildes: "concepcion" = "Concepción". */
    public static Optional<CotizacionEnvioDTO> cotizar(String comuna) {
        Tarifa t = TARIFAS.get(normalizar(comuna));
        if (t == null) return Optional.empty();
        CotizacionEnvioDTO dto = new CotizacionEnvioDTO();
        dto.setComuna(t.comuna());
        dto.setCosto(BigDecimal.valueOf(t.costo()));
        dto.setDiasMin(t.diasMin());
        dto.setDiasMax(t.diasMax());
        dto.setMensajeEntrega("Entrega estimada entre " + t.diasMin() + " y " + t.diasMax() + " días hábiles");
        return Optional.of(dto);
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
