package com.libreriajuanin.envios.util;

import com.libreriajuanin.envios.dto.CotizacionDTO;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Tarifas y plazos por comuna (envioService.ts). Valores de EJEMPLO: ajustar a las del transportista. */
public final class TarifasDespacho {

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

    private TarifasDespacho() {
    }

    private static void agregar(String comuna, int costo, int diasMin, int diasMax) {
        TARIFAS.put(normalizar(comuna), new Tarifa(comuna, costo, diasMin, diasMax));
    }

    /** Ignora mayúsculas y tildes: "concepcion" = "Concepción". */
    public static Optional<CotizacionDTO> cotizar(String comuna) {
        Tarifa t = TARIFAS.get(normalizar(comuna));
        if (t == null) return Optional.empty();
        CotizacionDTO dto = new CotizacionDTO();
        dto.setComuna(t.comuna());
        dto.setCosto(BigDecimal.valueOf(t.costo()));
        dto.setDiasMin(t.diasMin());
        dto.setDiasMax(t.diasMax());
        dto.setMensajeEntrega("Entrega estimada entre " + t.diasMin() + " y " + t.diasMax() + " días hábiles");
        return Optional.of(dto);
    }

    public static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
