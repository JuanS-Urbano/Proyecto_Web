package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.TipoGateway;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayDTO {

    private Long id;
    private String nombre;
    private TipoGateway tipoGateway;
    private Double posicionX;
    private Double posicionY;
    private ReferenciaDTO proceso;
}
