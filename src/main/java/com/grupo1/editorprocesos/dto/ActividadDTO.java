package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.TipoActividad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDTO {
    private Long id;

    @NotBlank(message = "El nombre de la actividad es requerido")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El tipo de actividad es requerido")
    private TipoActividad tipoActividad;

    private Double posicionX;
    private Double posicionY;

    @NotNull(message = "El proceso asociado es requerido")
    @Valid
    private ReferenciaDTO proceso;

    /**
     * ID del Lane al que pertenece esta actividad.
     * Al crear/editar una actividad, se valida que el laneId pertenezca
     * al mismo proceso que la actividad. Si laneId es null, la actividad
     * queda sin asignar a un lane (válido en estado BORRADOR).
     */
    private ReferenciaDTO lane;
}
