package com.grupo1.editorprocesos.model.entity.process;

import com.grupo1.editorprocesos.model.entity.audit.AuditableEntity;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "procesos")
@Getter
@Setter
public class Proceso extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoProceso estado = EstadoProceso.BORRADOR;

    @Column(name = "configuracion_compartido", nullable = false)
    private Boolean configuracionCompartido = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;
}
