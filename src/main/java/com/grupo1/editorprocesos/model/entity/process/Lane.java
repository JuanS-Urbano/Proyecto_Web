package com.grupo1.editorprocesos.model.entity.process;

import com.grupo1.editorprocesos.model.entity.audit.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lanes", indexes = {
        @Index(name = "idx_lane_proceso", columnList = "proceso_id"),
        @Index(name = "idx_lane_rol_proceso", columnList = "rol_proceso_id")
})
@Getter
@Setter
public class Lane extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "posicion_x")
    private Double posicionX;

    @Column(name = "posicion_y")
    private Double posicionY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_proceso_id", nullable = true)
    private RolProceso rolProceso;
}
