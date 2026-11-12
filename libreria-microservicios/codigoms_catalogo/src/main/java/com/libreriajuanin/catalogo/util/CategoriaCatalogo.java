package com.libreriajuanin.catalogo.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Categorías y subcategorías de la tienda (HU-23, HU-24, HU-26, HU-27). */
public final class CategoriaCatalogo {

    private static final Map<String, List<String>> ARBOL = new LinkedHashMap<>();

    static {
        ARBOL.put("Lápices", List.of("Grafito", "Lápiz pasta", "Destacadores"));
        ARBOL.put("Cuadernos", List.of("Universitarios", "Croqueras", "Escolares"));
        ARBOL.put("Papeles", List.of("Resmas", "Cartulinas"));
        ARBOL.put("Accesorios", List.of("Estuches", "Gomas y correctores"));
        ARBOL.put("Libros", List.of("Literatura"));
    }

    private CategoriaCatalogo() {
    }

    public static Map<String, List<String>> arbol() {
        return ARBOL;
    }

    public static boolean esCategoria(String categoria) {
        return categoria != null && ARBOL.containsKey(categoria);
    }

    public static boolean esSubcategoria(String categoria, String subcategoria) {
        return esCategoria(categoria) && ARBOL.get(categoria).contains(subcategoria);
    }
}
