package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.RolSistema;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String email;
    private RolSistema rolSistema;
    private Boolean isActivo;
    private Long empresaId;
}
// DTO que representa la información de un usuario (id, email, rol del sistema, estado activo y empresa a la que pertenece)