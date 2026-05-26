package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.MetricasDTO;
import com.grupo1.editorprocesos.service.MetricasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metricas")
@RequiredArgsConstructor
public class MetricasController {

    private final MetricasService metricasService;

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<ApiResponse<MetricasDTO>> obtenerMetricas(@PathVariable Long empresaId) {
        MetricasDTO metricas = metricasService.obtenerMetricasPorEmpresa(empresaId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Métricas obtenidas exitosamente", metricas));
    }
}
