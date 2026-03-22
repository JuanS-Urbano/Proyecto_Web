package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.MensajeDTO;

import java.util.List;

/**
 * Contrato del servicio de mensajería BPMN.
 *
 * Scope HU-27 (Dev 2): solo catchMessage().
 * throwMessage() se declara aquí para que el proyecto compile,
 * pero su implementación pertenece a Dev 1 (HU-25).
 */
public interface MensajeService {

    /**
     * HU-27 — Captura un mensaje BPMN entrante y lo integra al flujo del proceso destino.
     *
     * ─── Cruce con Dev 1 ─────────────────────────────────────────────────
     * Dev 1 invoca este método (o el endpoint POST /api/v1/mensajes/catch)
     * después de persistir su throwMessage, pasando el mismo nombre de mensaje
     * y el ID del proceso destino.
     * ─────────────────────────────────────────────────────────────────────
     *
     * @param mensajeDTO datos del mensaje entrante
     * @return MensajeDTO con el estado del mensaje capturado y persistido
     */
    MensajeDTO catchMessage(MensajeDTO mensajeDTO);

    /**
     * Obtiene un mensaje por su ID.
     */
    MensajeDTO obtenerMensajePorId(Long id);

    /**
     * Lista todos los mensajes CATCH de un proceso destino.
public interface MensajeService {

    /**
     * HU-25 (Dev 1): Envía un Message Throw. Valida JSON del payload si se proporciona.
     */
    MensajeDTO throwMessage(MensajeDTO dto);

    /**
     * Lista todos los mensajes de un proceso.
     */
    List<MensajeDTO> listarMensajesPorProceso(Long procesoId);

    /**
     * HU-25 — Lanza un mensaje desde un proceso origen.
     * IMPLEMENTACIÓN PENDIENTE: Dev 1 (HU-25).
     * Se declara aquí para mantener cohesión en la interfaz del servicio.
     */
    MensajeDTO throwMessage(MensajeDTO mensajeDTO);
     * Obtiene un mensaje por su ID.
     */
    MensajeDTO obtenerMensajePorId(Long id);
}
