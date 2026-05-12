package com.grupo1.editorprocesos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArcoDTO {
    private Long id;

    @NotBlank(message = "El origenId del arco es requerido")
    private String origenId; // ID del elemento origen (Actividad, Gateway, etc.)

    @NotBlank(message = "El destinoId del arco es requerido")
    private String destinoId; // ID del elemento destino (Actividad, Gateway, etc.)

    @NotNull(message = "El proceso asociado es requerido")
    @Valid
    private ReferenciaDTO proceso;
}