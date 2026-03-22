package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.TipoActividad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private TipoActividad tipoActividad;
    private Double posicionX;
    private Double posicionY;
    private Long procesoId;

    /**
     * ID del Lane al que pertenece esta actividad.
     * Al crear/editar una actividad, se valida que el laneId pertenezca
     * al mismo proceso que la actividad. Si laneId es null, la actividad
     * queda sin asignar a un lane (válido en estado BORRADOR).
     */
    private Long laneId;
}
