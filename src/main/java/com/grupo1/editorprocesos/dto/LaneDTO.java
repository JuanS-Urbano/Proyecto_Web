package com.grupo1.editorprocesos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaneDTO {
    private Long id;

    @NotBlank(message = "El nombre del lane es requerido")
    private String nombre;

    private String descripcion;
    private Integer orden;
    private Double posicionX;
    private Double posicionY;
    // proceso viene del PathVariable en LaneController, no se valida aquí
    private ReferenciaDTO proceso;
    private ReferenciaDTO rolProceso;
}
