package com.grupo1.editorprocesos.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private Long usuarioId;
    private String email;
}
