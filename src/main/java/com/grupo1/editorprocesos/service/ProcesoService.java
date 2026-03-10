package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.ProcesoDTO;
import java.util.List;

public interface ProcesoService {
    
    /**
     * Crea un nuevo proceso asociado a la empresa y pool del usuario actual.
     * Valida que el usuario pertenezca a la empresa del pool especificado.
     * 
     * @param procesoDTO DTO con los datos del proceso a crear
     * @return DTO del proceso creado con su ID asignado
     * @throws com.grupo1.editorprocesos.exception.ResourceNotFoundException si la empresa o pool no existen
     * @throws com.grupo1.editorprocesos.exception.UnauthorizedException si el usuario no pertenece a la empresa
     */
    ProcesoDTO crearProceso(ProcesoDTO procesoDTO);

    ProcesoDTO obtenerProcesoById(Long id);

    List<ProcesoDTO> listarProcesosPorEmpresa(Long empresaId);

    List<ProcesoDTO> listarProcesosPorPool(Long poolId);

    ProcesoDTO editarProceso(Long id, ProcesoDTO procesoDTO);

    void eliminarProceso(Long id);
}
