package com.grupo1.editorprocesos.model.entity.core;

import com.grupo1.editorprocesos.model.entity.audit.AuditableEntity;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pools")
@Getter
@Setter
public class Pool extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "pool", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Proceso> procesos = new ArrayList<>();
}
