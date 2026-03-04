package com.grupo1.editorprocesos.model.entity.core;

import com.grupo1.editorprocesos.model.entity.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "empresas")
@Getter
@Setter
public class Empresa extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nit;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "correo_contacto", nullable = false, length = 100)
    private String correoContacto;
}
