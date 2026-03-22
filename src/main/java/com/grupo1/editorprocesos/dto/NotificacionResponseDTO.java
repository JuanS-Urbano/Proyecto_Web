package com.grupo1.editorprocesos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HU-26: Respuesta del servicio de notificaciones externas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponseDTO {

    private boolean enviado;
    private String tipo;
    private String destino;
    private String mensaje;
}
