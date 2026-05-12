package com.grupo1.editorprocesos.dto;

import com.grupo1.editorprocesos.model.enums.TipoGateway;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayDTO {

    private Long id;

    @NotBlank(message = "El nombre del gateway es requerido")
    private String nombre;

    @NotNull(message = "El tipo de gateway es requerido")
    private TipoGateway tipoGateway;

    private Double posicionX;
    private Double posicionY;

    @NotNull(message = "El proceso asociado es requerido")
    @Valid
    private ReferenciaDTO proceso;
}
