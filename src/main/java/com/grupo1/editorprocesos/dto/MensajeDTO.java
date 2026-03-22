package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
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
    private String nombre;
    private String payloadJson;
    private TipoMensaje tipo;
    private EstadoMensaje estado;
    private Long procesoDestinoId;
    private String correlationKey;
    private Long procesoId;
    private Long actividadOrigenId;
}
