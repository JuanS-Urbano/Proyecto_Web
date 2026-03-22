package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.NotificacionRequestDTO;
import com.grupo1.editorprocesos.dto.NotificacionResponseDTO;
import com.grupo1.editorprocesos.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HU-26: Endpoints para disparar notificaciones externas desde actividades.
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * Envía una notificación externa (EMAIL o WEBHOOK).
     */
    @PostMapping("/enviar")
    public ResponseEntity<ApiResponse<NotificacionResponseDTO>> enviar(
            @RequestBody NotificacionRequestDTO request) {
        NotificacionResponseDTO resultado = notificacionService.enviar(request);
        String msg = resultado.isEnviado() ? "Notificación enviada" : "Error al enviar la notificación";
        return ResponseEntity.ok(new ApiResponse<>(resultado.isEnviado(), msg, resultado));
    }

    /**
     * Envía directamente un correo electrónico.
     */
    @PostMapping("/email")
    public ResponseEntity<ApiResponse<NotificacionResponseDTO>> enviarEmail(
            @RequestParam String destino,
            @RequestParam String asunto,
            @RequestParam String cuerpo) {
        NotificacionResponseDTO resultado = notificacionService.enviarEmail(destino, asunto, cuerpo);
        return ResponseEntity.ok(new ApiResponse<>(resultado.isEnviado(), resultado.getMensaje(), resultado));
    }

    /**
     * Envía directamente un webhook a una URL externa.
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<NotificacionResponseDTO>> enviarWebhook(
            @RequestParam String url,
            @RequestParam String payload) {
        NotificacionResponseDTO resultado = notificacionService.enviarWebhook(url, payload);
        return ResponseEntity.ok(new ApiResponse<>(resultado.isEnviado(), resultado.getMensaje(), resultado));
    }
}
