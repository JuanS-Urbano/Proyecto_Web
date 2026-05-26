package com.grupo1.editorprocesos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricasDTO {
    private Long totalProcesos;
    private Map<String, Long> procesosPorEstado;
    private Long totalUsuarios;
    private Map<String, Long> usuariosPorRol;
    private Long totalActividades;
    private Long totalRolesProceso;
    private List<HistorialCambiosDTO> ultimosCambios;
}
