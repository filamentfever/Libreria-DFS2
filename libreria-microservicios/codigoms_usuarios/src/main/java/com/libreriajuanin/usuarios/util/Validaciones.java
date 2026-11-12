package com.libreriajuanin.usuarios.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de validación compartidas (RUT chileno, correo, teléfono y contraseña). */
public final class Validaciones {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern TELEFONO = Pattern.compile("^\\+?[\\d\\s]{8,15}$");

    private Validaciones() {
    }

    public static boolean emailValido(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    /** El teléfono es opcional: vacío es válido. */
    public static boolean telefonoValido(String telefono) {
        return telefono == null || telefono.isBlank() || TELEFONO.matcher(telefono.trim()).matches();
    }

    /** Deja solo dígitos y K en mayúscula: "11.111.111-1" → "111111111". */
    public static String limpiarRut(String rut) {
        return rut == null ? "" : rut.replaceAll("[^0-9kK]", "").toUpperCase();
    }

    /** "111111111" → "11.111.111-1". */
    public static String formatearRut(String rutLimpio) {
        if (rutLimpio == null || rutLimpio.length() < 2) return rutLimpio;
        String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
        String dv = rutLimpio.substring(rutLimpio.length() - 1);
        String conPuntos = new StringBuilder(cuerpo).reverse().toString().replaceAll("(\\d{3})(?=\\d)", "$1.");
        return new StringBuilder(conPuntos).reverse() + "-" + dv;
    }

    /** Dígito verificador por módulo 11. */
    public static boolean rutValido(String rut) {
        String limpio = limpiarRut(rut);
        if (limpio.length() < 2) return false;
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        if (!cuerpo.matches("\\d+")) return false;
        int suma = 0;
        int multiplo = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplo;
            multiplo = multiplo == 7 ? 2 : multiplo + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return dv == esperado;
    }

    /** Lista las reglas de contraseña que NO se cumplen (vacía = contraseña válida). */
    public static List<String> reglasPasswordIncumplidas(String password) {
        List<String> faltan = new ArrayList<>();
        String p = password == null ? "" : password;
        if (p.length() < 8) faltan.add("mínimo 8 caracteres");
        if (!p.matches(".*[A-Z].*")) faltan.add("una mayúscula");
        if (!p.matches(".*[a-z].*")) faltan.add("una minúscula");
        if (!p.matches(".*\\d.*")) faltan.add("un número");
        if (!p.matches(".*[^A-Za-z0-9].*")) faltan.add("un carácter especial");
        return faltan;
    }

    public static boolean passwordValida(String password) {
        return reglasPasswordIncumplidas(password).isEmpty();
    }
}
