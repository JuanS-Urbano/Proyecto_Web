package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.HistorialCambiosDTO;
import com.grupo1.editorprocesos.dto.ProcesoDTO;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;

import java.util.List;

public interface ProcesoService {

    ProcesoDTO crearProceso(ProcesoDTO procesoDTO);

    ProcesoDTO obtenerProcesoById(Long id);

    List<ProcesoDTO> listarProcesosPorEmpresa(Long empresaId);

    List<ProcesoDTO> listarProcesosPorPool(Long poolId);

    List<ProcesoDTO> listarProcesosPorPoolYEstado(Long poolId, EstadoProceso estado);

    List<ProcesoDTO> listarProcesosPorPoolYCategoria(Long poolId, String categoria);

    List<ProcesoDTO> listarProcesosPorPoolConFiltros(Long poolId, EstadoProceso estado, String categoria);

    List<ProcesoDTO> buscarProcesosPorNombre(Long poolId, String nombre);

    ProcesoDTO editarProceso(Long id, ProcesoDTO procesoDTO);

    void eliminarProceso(Long id);

    List<HistorialCambiosDTO> obtenerHistorialProceso(Long procesoId);
}
