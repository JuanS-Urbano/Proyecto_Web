package com.grupo1.editorprocesos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaneDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer orden;
    private Double posicionX;
    private Double posicionY;
    private Long procesoId;
    private Long rolProcesoId;
}
