package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {

    private Long id;

    @NotBlank(message = "El nombre del mensaje es requerido")
    private String nombre;

    private String payloadJson;

    @NotNull(message = "El tipo de mensaje es requerido")
    private TipoMensaje tipo;

    private EstadoMensaje estado;
    private String correlationKey;

    /** Dev 1 (HU-25): Proceso que genera el THROW */
    private ReferenciaDTO proceso;

    /** Dev 1 (HU-25): Actividad origen del throw */
    private ReferenciaDTO actividadOrigen;

    /** Dev 2 (HU-27): Proceso destino del CATCH */
    private ReferenciaDTO procesoDestino;
}
