package com.libreriajuanin.usuarios.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Pruebas de las reglas puras (no necesitan mocks). */
class ValidacionesTest {

    @Test
    void rutValido_conDigitoVerificadorCorrecto_esTrue() {
        assertThat(Validaciones.rutValido("11.111.111-1")).isTrue();
        assertThat(Validaciones.rutValido("12.345.678-5")).isTrue();
    }

    @Test
    void rutValido_conDigitoVerificadorIncorrecto_esFalse() {
        assertThat(Validaciones.rutValido("11.111.111-9")).isFalse();
        assertThat(Validaciones.rutValido("1-1")).isFalse();
        assertThat(Validaciones.rutValido(null)).isFalse();
    }

    @Test
    void formatearRut_agregaPuntosYGuion() {
        assertThat(Validaciones.formatearRut("111111111")).isEqualTo("11.111.111-1");
    }

    @Test
    void passwordValida_exigeTodasLasReglas() {
        assertThat(Validaciones.passwordValida("Clave123!")).isTrue();
        assertThat(Validaciones.passwordValida("clave123!")).isFalse(); // sin mayúscula
        assertThat(Validaciones.passwordValida("Clave!!!!")).isFalse(); // sin número
        assertThat(Validaciones.reglasPasswordIncumplidas("abc")).hasSize(4);
    }

    @Test
    void emailYTelefono_validanFormato() {
        assertThat(Validaciones.emailValido("ana@mail.cl")).isTrue();
        assertThat(Validaciones.emailValido("ana@mail")).isFalse();
        assertThat(Validaciones.telefonoValido("")).isTrue(); // opcional
        assertThat(Validaciones.telefonoValido("+56 9 1234 5678")).isTrue();
        assertThat(Validaciones.telefonoValido("abc")).isFalse();
    }
}
