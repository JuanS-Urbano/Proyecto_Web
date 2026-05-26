package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.LaneDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.entity.process.RolProceso;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.LaneRepository;
import com.grupo1.editorprocesos.service.ProcesoService;
import com.grupo1.editorprocesos.service.RolProcesoService;
import com.grupo1.editorprocesos.service.LaneService;
import com.grupo1.editorprocesos.service.PermisosPoolService;
import com.grupo1.editorprocesos.service.UsuarioActualService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SuppressWarnings({"java:S6809", "java:S6204"})
@Service
@RequiredArgsConstructor
public class LaneServiceImpl implements LaneService {

    private final LaneRepository laneRepository;
    private final ProcesoService procesoService;
    private final RolProcesoService rolProcesoService;
    private final ActividadRepository actividadRepository;
    private final UsuarioActualService usuarioActualService;
    private final PermisosPoolService permisosPoolService;

    @Override
    @Transactional
    public LaneDTO crearLane(Long procesoId, LaneDTO laneDTO) {
        Proceso proceso = procesoService.obtenerEntityById(procesoId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());
        permisosPoolService.validarPermisoEscritura(usuarioActual);

        if (laneDTO.getNombre() == null || laneDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del lane es requerido");
        }

        Lane lane = new Lane();
        lane.setNombre(laneDTO.getNombre());
        lane.setDescripcion(laneDTO.getDescripcion());
        lane.setOrden(laneDTO.getOrden());
        lane.setPosicionX(laneDTO.getPosicionX());
        lane.setPosicionY(laneDTO.getPosicionY());
        lane.setProceso(proceso);

        if (laneDTO.getRolProceso() != null && laneDTO.getRolProceso().getId() != null) {
            RolProceso rolProceso = rolProcesoService.obtenerEntityById(laneDTO.getRolProceso().getId());

            Empresa empresaProceso = proceso.getPool().getEmpresa();
            if (rolProceso.getEmpresa() == null || !rolProceso.getEmpresa().getId().equals(empresaProceso.getId())) {
                throw new IllegalArgumentException(
                        "El RolProceso no pertenece a la misma empresa del proceso");
            }

            lane.setRolProceso(rolProceso);
        }

        Lane guardado = laneRepository.save(lane);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaneDTO> listarLanesPorProceso(Long procesoId) {
        Proceso proceso = procesoService.obtenerEntityById(procesoId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return laneRepository.findByProcesoId(procesoId).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    public LaneDTO obtenerLanePorId(Long laneId) {
        Lane lane = obtenerLaneEntity(laneId);
        return convertirADTO(lane);
    }

    @Override
    public Lane obtenerLaneEntityById(Long laneId) {
        return obtenerLaneEntity(laneId);
    }

    private Lane obtenerLaneEntity(Long laneId) {
        return laneRepository.findById(laneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lane no encontrado con ID: " + laneId));
    }

    @Override
    public void validarLanePerteneceAlProceso(Long laneId, Long procesoId) {
        Lane lane = obtenerLaneEntity(laneId);
        if (lane.getProceso() == null || !lane.getProceso().getId().equals(procesoId)) {
            throw new IllegalArgumentException(
                    String.format("El lane con ID %d no pertenece al proceso %d", laneId, procesoId));
        }
    }

    // =====================================================================================
    // Editar Lane
    // =====================================================================================

    @Override
    @Transactional
    public LaneDTO editarLane(Long laneId, LaneDTO laneDTO) {
        Lane lane = obtenerLaneEntity(laneId);

        Proceso proceso = lane.getProceso();
        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());
        permisosPoolService.validarPermisoEscritura(usuarioActual);

        if (laneDTO.getNombre() != null && !laneDTO.getNombre().isBlank()) {
            lane.setNombre(laneDTO.getNombre());
        }
        if (laneDTO.getDescripcion() != null) {
            lane.setDescripcion(laneDTO.getDescripcion());
        }
        if (laneDTO.getOrden() != null) {
            lane.setOrden(laneDTO.getOrden());
        }
        if (laneDTO.getPosicionX() != null) {
            lane.setPosicionX(laneDTO.getPosicionX());
        }
        if (laneDTO.getPosicionY() != null) {
            lane.setPosicionY(laneDTO.getPosicionY());
        }

        // Cambiar RolProceso si se proporciona
        if (laneDTO.getRolProceso() != null && laneDTO.getRolProceso().getId() != null) {
            RolProceso rolProceso = rolProcesoService.obtenerEntityById(laneDTO.getRolProceso().getId());

            Empresa empresaProceso = proceso.getPool().getEmpresa();
            if (rolProceso.getEmpresa() == null || !rolProceso.getEmpresa().getId().equals(empresaProceso.getId())) {
                throw new IllegalArgumentException(
                        "El RolProceso no pertenece a la misma empresa del proceso");
            }
            lane.setRolProceso(rolProceso);
        }

        Lane actualizado = laneRepository.save(lane);
        return convertirADTO(actualizado);
    }

    // =====================================================================================
    // Eliminar Lane — valida que no tenga actividades asignadas
    // =====================================================================================

    @Override
    @Transactional
    public void eliminarLane(Long laneId) {
        Lane lane = obtenerLaneEntity(laneId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, lane.getProceso().getPool().getEmpresa());
        permisosPoolService.validarPermisoEscritura(usuarioActual);

        // Validar que no tenga actividades asignadas
        var actividadesEnLane = actividadRepository.findByLaneId(laneId);
        if (!actividadesEnLane.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el lane con ID " + laneId
                            + " porque tiene " + actividadesEnLane.size()
                            + " actividad(es) asignada(s). Reasígnelas antes de eliminar.");
        }

        laneRepository.delete(lane);
    }

    private LaneDTO convertirADTO(Lane lane) {
        LaneDTO dto = new LaneDTO();
        dto.setId(lane.getId());
        dto.setNombre(lane.getNombre());
        dto.setDescripcion(lane.getDescripcion());
        dto.setOrden(lane.getOrden());
        dto.setPosicionX(lane.getPosicionX());
        dto.setPosicionY(lane.getPosicionY());
        dto.setProceso(lane.getProceso() != null ? new com.grupo1.editorprocesos.dto.ReferenciaDTO(lane.getProceso().getId(), lane.getProceso().getNombre()) : null);
        dto.setRolProceso(lane.getRolProceso() != null ? new com.grupo1.editorprocesos.dto.ReferenciaDTO(lane.getRolProceso().getId(), lane.getRolProceso().getNombre()) : null);
        return dto;
    }



    private void validarUsuarioPertenecAEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario.getEmpresa() == null
                || !usuario.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException(
                    "El usuario no tiene acceso a la empresa con ID: " + empresa.getId());
        }
    }
}
