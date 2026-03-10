package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.RolSistema;

public class UsuarioEmpresaDTO {

    private String nombre;
    private String correo;
    private RolSistema rol;
    private Long empresaId;

    public UsuarioEmpresaDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public RolSistema getRol() {
        return rol;
    }

    public void setRol(RolSistema rol) {
        this.rol = rol;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
}