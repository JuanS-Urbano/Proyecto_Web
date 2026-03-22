package com.grupo1.editorprocesos.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la entidad Mensaje (HU-27 — Message Catch).
 *
 * ─── Cruce Throw/Catch con Dev 1 ──────────────────────────────────────────
 * Dev 1 (throwMessage) debe enviar un POST a /api/v1/mensajes/catch con:
 *   - nombre         : nombre del mensaje BPMN (ej. "msgPagoAprobado")
 *   - tipo           : "CATCH"
 *   - payloadJson    : JSON opcional con variables de negocio (puede ser null)
 *   - procesoOrigenId: ID del proceso que lanza el mensaje (lo conoce Dev 1)
 *   - procesoDestinoId: ID del proceso receptor (acordado en diseño BPMN)
 * ─────────────────────────────────────────────────────────────────────────
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {

    private Long id;

    /** Nombre del evento de mensaje BPMN (ej. "msgPagoAprobado"). Obligatorio. */
    private String nombre;

    /**
     * Payload JSON opcional con variables de negocio.
     * Dev 1 puede enviar null si no necesita pasar datos adicionales.
     * Dev 3 (HU-28) ampliará la correlación usando estos datos.
     */
    private String payloadJson;

    /**
     * Tipo del evento: "THROW" (Dev 1) o "CATCH" (Dev 2 — este servicio).
     */
    private String tipo;

    /** ID del proceso que origina el mensaje. Provisto por Dev 1. */
    private Long procesoOrigenId;

    /** ID del proceso destino que debe recibir el mensaje. */
    private Long procesoDestinoId;
}
