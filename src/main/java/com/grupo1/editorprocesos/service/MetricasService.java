package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.MetricasDTO;

public interface MetricasService {
    MetricasDTO obtenerMetricasPorEmpresa(Long empresaId);
}
