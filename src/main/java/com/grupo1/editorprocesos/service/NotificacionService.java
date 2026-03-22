package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.NotificacionRequestDTO;
import com.grupo1.editorprocesos.dto.NotificacionResponseDTO;

/**
 * HU-26: Servicio para enviar notificaciones externas desde actividades.
 * Soporta correo electrónico y webhooks con manejo de errores.
 */
public interface NotificacionService {

    /** Envía una notificación según el tipo (EMAIL o WEBHOOK). */
    NotificacionResponseDTO enviar(NotificacionRequestDTO request);

    /** Envía un correo electrónico (mock en Fase 1). */
    NotificacionResponseDTO enviarEmail(String destino, String asunto, String cuerpo);

    /** Envía un webhook HTTP POST a una URL externa (mock en Fase 1). */
    NotificacionResponseDTO enviarWebhook(String url, String payload);
}
