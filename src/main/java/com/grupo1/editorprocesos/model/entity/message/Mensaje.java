package com.grupo1.editorprocesos.model.entity.message;

import com.grupo1.editorprocesos.model.entity.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un mensaje BPMN (IntermediateThrowEvent / IntermediateCatchEvent).
 *
 * Cambios respecto al modelo original para HU-27:
 *  - Se añaden procesoOrigenId y procesoDestinoId para vincular el flujo Throw/Catch
 *    sin romper la estructura existente de la entidad.
 *
 * ─── Nota para Dev 3 (HU-28) ─────────────────────────────────────────────
 * La correlación avanzada por businessKey puede añadirse aquí como nuevo campo
 * sin afectar catchMessage() ni throwMessage().
 * ─────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "mensajes")
@Getter
@Setter
public class Mensaje extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del evento de mensaje BPMN. Coincide entre Throw y Catch. */
    @Column(nullable = false, length = 150)
    private String nombre;

    /** Payload JSON opcional con variables de negocio. */
    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    /** "THROW" (Dev 1 / HU-25) o "CATCH" (Dev 2 / HU-27). */
    @Column(nullable = false, length = 50)
    private String tipo;

    /**
     * ID del proceso que origina el mensaje (proceso lanzador).
     * Llenado por Dev 1 en throwMessage().
     */
    @Column(name = "proceso_origen_id")
    private Long procesoOrigenId;

    /**
     * ID del proceso que debe recibir el mensaje.
     * Llenado por Dev 2 en catchMessage().
     */
    @Column(name = "proceso_destino_id")
    private Long procesoDestinoId;
}
