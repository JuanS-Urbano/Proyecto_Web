package com.grupo1.editorprocesos.model.entity.bpmn;

import com.grupo1.editorprocesos.model.entity.audit.AuditableEntity;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "arcos")
@Getter
@Setter
public class Arco extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origen_id", nullable = false, length = 100)
    private String origenId; // Reference to start element (UUID or ID)

    @Column(name = "destino_id", nullable = false, length = 100)
    private String destinoId; // Reference to target element (UUID or ID)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;
}
