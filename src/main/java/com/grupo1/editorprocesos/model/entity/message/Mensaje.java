package com.grupo1.editorprocesos.model.entity.message;

import com.grupo1.editorprocesos.model.entity.audit.AuditableEntity;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mensajes", indexes = {
        @Index(name = "idx_mensaje_correlation_key", columnList = "correlation_key"),
        @Index(name = "idx_mensaje_proceso", columnList = "proceso_id"),
        @Index(name = "idx_mensaje_proceso_destino", columnList = "proceso_destino_id"),
        @Index(name = "idx_mensaje_estado", columnList = "estado")
})
@Getter
@Setter
public class Mensaje extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoMensaje tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoMensaje estado = EstadoMensaje.PENDIENTE;

    @Column(name = "correlation_key", length = 255)
    private String correlationKey;

    /** Dev 1 (HU-25): Proceso que origina el THROW */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id")
    private Proceso proceso;

    /** Dev 1 (HU-25): Actividad que genera el THROW */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_origen_id")
    private Actividad actividadOrigen;

    /** Dev 2 (HU-27): Proceso destino del CATCH */
    @Column(name = "proceso_destino_id")
    private Long procesoDestinoId;
}
