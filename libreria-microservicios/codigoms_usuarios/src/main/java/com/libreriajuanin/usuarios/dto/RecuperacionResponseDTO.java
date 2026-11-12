package com.libreriajuanin.usuarios.dto;

public class RecuperacionResponseDTO {

    private String mensaje;
    private String passwordTemporal;

    public RecuperacionResponseDTO() {
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPasswordTemporal() {
        return passwordTemporal;
    }

    public void setPasswordTemporal(String passwordTemporal) {
        this.passwordTemporal = passwordTemporal;
    }

}
