package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import lombok.Data;

@Data
public class ProcesoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private EstadoProceso estado;
    private Boolean configuracionCompartido;
    private Long poolId;
}
