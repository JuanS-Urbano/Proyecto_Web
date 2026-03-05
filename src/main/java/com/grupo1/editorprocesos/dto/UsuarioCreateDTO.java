package com.grupo1.editorprocesos.dto;

import lombok.Data;

@Data
public class UsuarioCreateDTO {
    private String email;
    private String password; // temporal, se puede enviar correo para establecer
    private Long empresaId;
}
// DTO que transporta los datos necesarios (email, contraseña y empresaId) para crear o invitar un usuario dentro de una empresa