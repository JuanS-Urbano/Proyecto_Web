package com.grupo1.editorprocesos.dto;

import lombok.Data;

/**
 * HU-26: DTO con los datos necesarios para enviar una notificación externa
 * (correo o webhook) desde una actividad del proceso.
 */
@Data
public class NotificacionRequestDTO {

    /** Tipo de notificación: EMAIL o WEBHOOK */
    private String tipo;

    /** Destinatario: correo electrónico o URL del webhook */
    private String destino;

    /** Asunto del correo o descripción del evento */
    private String asunto;

    /** Cuerpo del mensaje o payload JSON para el webhook */
    private String cuerpo;

    /** ID de la actividad que dispara la notificación (opcional) */
    private Long actividadId;
}
