package com.grupo1.editorprocesos.model.entity.bpmn;

import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.enums.TipoActividad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "actividades")
@Getter
@Setter
public class Actividad extends ElementoBpmn {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actividad", nullable = false, length = 50)
    private TipoActividad tipoActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id")
    private Lane lane;
}
