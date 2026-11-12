package com.libreriajuanin.notificaciones.dto;

public class RegistroUsuarioRequestDTO {

    private String nombre;
    private String email;

    public RegistroUsuarioRequestDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
