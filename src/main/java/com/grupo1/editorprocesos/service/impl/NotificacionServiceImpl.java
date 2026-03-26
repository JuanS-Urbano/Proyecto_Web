package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.NotificacionRequestDTO;
import com.grupo1.editorprocesos.dto.NotificacionResponseDTO;
import com.grupo1.editorprocesos.service.NotificacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * HU-26: Implementación con mocks de envío de correo y webhook.
 * En la Fase 3 se reemplazarán los mocks por conectores reales
 * (JavaMailSender para email, RestTemplate/WebClient para webhooks).
 */
@Slf4j
@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final String TIPO_EMAIL = "EMAIL";
    private static final String TIPO_WEBHOOK = "WEBHOOK";

    @Override
    public NotificacionResponseDTO enviar(NotificacionRequestDTO request) {
        if (request.getTipo() == null || request.getTipo().isBlank()) {
            return new NotificacionResponseDTO(false, null, request.getDestino(),
                    "El tipo de notificación es obligatorio (EMAIL o WEBHOOK).");
        }

        return switch (request.getTipo().toUpperCase()) {
            case TIPO_EMAIL -> enviarEmail(request.getDestino(), request.getAsunto(), request.getCuerpo());
            case TIPO_WEBHOOK -> enviarWebhook(request.getDestino(), request.getCuerpo());
            default -> new NotificacionResponseDTO(false, request.getTipo(), request.getDestino(),
                    "Tipo de notificación no soportado: " + request.getTipo());
        };
    }

    @Override
    public NotificacionResponseDTO enviarEmail(String destino, String asunto, String cuerpo) {
        try {
            validarDestino(destino, TIPO_EMAIL);

            // MOCK: simula el envío de correo
            // En Fase 3 se conectará con JavaMailSender
            log.info("[MOCK EMAIL] Para: {} | Asunto: {} | Cuerpo: {}", destino, asunto, cuerpo);

            return new NotificacionResponseDTO(true, TIPO_EMAIL, destino,
                    "Correo enviado exitosamente a: " + destino);

        } catch (IllegalArgumentException e) {
            log.warn("[EMAIL] Validación fallida: {}", e.getMessage());
            return new NotificacionResponseDTO(false, TIPO_EMAIL, destino, e.getMessage());

        } catch (Exception e) {
            log.error("[EMAIL] Error inesperado al enviar a {}: {}", destino, e.getMessage());
            return new NotificacionResponseDTO(false, TIPO_EMAIL, destino,
                    "Error al enviar el correo. Se registró para reintento.");
        }
    }

    @Override
    public NotificacionResponseDTO enviarWebhook(String url, String payload) {
        try {
            validarDestino(url, TIPO_WEBHOOK);
            validarUrl(url);

            // MOCK: simula el POST HTTP al webhook
            // En Fase 3 se conectará con RestTemplate o WebClient
            log.info("[MOCK WEBHOOK] URL: {} | Payload: {}", url, payload);

            return new NotificacionResponseDTO(true, TIPO_WEBHOOK, url,
                    "Webhook enviado exitosamente a: " + url);

        } catch (IllegalArgumentException e) {
            log.warn("[WEBHOOK] Validación fallida: {}", e.getMessage());
            return new NotificacionResponseDTO(false, TIPO_WEBHOOK, url, e.getMessage());

        } catch (Exception e) {
            log.error("[WEBHOOK] Error inesperado al enviar a {}: {}", url, e.getMessage());
            return new NotificacionResponseDTO(false, TIPO_WEBHOOK, url,
                    "Error al enviar el webhook. Se registró para reintento.");
        }
    }

    // -------------------------------------------------------------------
    // Validaciones privadas
    // -------------------------------------------------------------------

    private void validarDestino(String destino, String tipo) {
        if (destino == null || destino.isBlank()) {
            throw new IllegalArgumentException(
                    "El destino no puede estar vacío para una notificación de tipo " + tipo + ".");
        }
    }

    private void validarUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException(
                    "La URL del webhook debe comenzar con http:// o https://.");
        }
    }
}
