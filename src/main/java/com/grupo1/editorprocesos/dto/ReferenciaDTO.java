package com.grupo1.editorprocesos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferenciaDTO {

    @NotNull(message = "El id de la referencia es requerido")
    private Long id;

    private String nombre;
}
