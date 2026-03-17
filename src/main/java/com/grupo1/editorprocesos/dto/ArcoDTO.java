package com.grupo1.editorprocesos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArcoDTO {
    private Long id;
    private String origenId; // ID del elemento origen (Actividad, Gateway, etc.)
    private String destinoId; // ID del elemento destino (Actividad, Gateway, etc.)
    private Long procesoId;
}