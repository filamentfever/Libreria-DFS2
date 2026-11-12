package com.libreriajuanin.usuarios;

import com.libreriajuanin.usuarios.dto.RegistroRequestDTO;
import com.libreriajuanin.usuarios.model.Usuario;
import java.util.ArrayList;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    /** Formulario de registro válido (RUT 11.111.111-1, contraseña con todas las reglas). */
    public static RegistroRequestDTO unRegistro() {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setNombre("Ana Pérez");
        dto.setRut("11.111.111-1");
        dto.setEmail("ana@mail.cl");
        dto.setTelefono("+56 9 1234 5678");
        dto.setPassword("Clave123!");
        dto.setPasswordConfirmacion("Clave123!");
        return dto;
    }

    public static Usuario unUsuario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("ana");
        u.setNombre("Ana Pérez");
        u.setRut("111111111");
        u.setEmail("ana@mail.cl");
        u.setPasswordHash("hash-guardado");
        u.setDirecciones(new ArrayList<>());
        return u;
    }
}
