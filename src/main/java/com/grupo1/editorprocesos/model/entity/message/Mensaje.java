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
@Table(name = "mensajes")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_origen_id")
    private Actividad actividadOrigen;
}
