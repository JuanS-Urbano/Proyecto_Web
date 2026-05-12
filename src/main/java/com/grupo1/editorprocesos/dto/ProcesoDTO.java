package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoDTO {
    private Long id;

    @NotBlank(message = "El nombre del proceso es requerido")
    private String nombre;

    private String descripcion;
    private String categoria;
    private EstadoProceso estado;
    private Boolean configuracionCompartido;

    @NotNull(message = "El pool asociado es requerido")
    @Valid
    private ReferenciaDTO pool;

    private ReferenciaDTO empresa;
}
