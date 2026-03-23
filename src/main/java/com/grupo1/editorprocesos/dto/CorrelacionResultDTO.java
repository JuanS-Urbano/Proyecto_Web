package com.grupo1.editorprocesos.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-28 (Dev 3): Resultado de la correlación de mensajes THROW↔CATCH.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorrelacionResultDTO {

    private MensajeDTO throwMensaje;
    private MensajeDTO catchMensaje;
    private boolean correlacionExitosa;
    private String mensaje;
}
