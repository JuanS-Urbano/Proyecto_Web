package com.grupo1.editorprocesos.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private ReferenciaDTO usuario;
    private String email;
    private String rolSistema;
    private ReferenciaDTO empresa;
}
