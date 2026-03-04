package com.grupo1.editorprocesos.model.entity.bpmn;

import com.grupo1.editorprocesos.model.enums.TipoGateway;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gateways")
@Getter
@Setter
public class Gateway extends ElementoBpmn {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_gateway", nullable = false, length = 50)
    private TipoGateway tipoGateway;
}
