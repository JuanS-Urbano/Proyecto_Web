package com.grupo1.editorprocesos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCambiosDTO {
    private Long id;
    private ReferenciaDTO proceso;
    private ReferenciaDTO usuario;
    private LocalDateTime fecha;
    private String cambio;
}
