package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MensajeDTO {

    private Long id;
    private String nombre;
    private String payloadJson;
    private TipoMensaje tipo;
    private EstadoMensaje estado;
    private String correlationKey;
    private Long procesoId;
    private Long actividadOrigenId;
}
