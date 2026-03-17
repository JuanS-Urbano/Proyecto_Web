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
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.RolProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.LaneService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaneServiceImpl implements LaneService {

    private final LaneRepository laneRepository;
    private final ProcesoRepository procesoRepository;
    private final RolProcesoRepository rolProcesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ActividadRepository actividadRepository;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public LaneDTO crearLane(Long procesoId, LaneDTO laneDTO) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + procesoId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

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

        if (laneDTO.getRolProcesoId() != null) {
            RolProceso rolProceso = rolProcesoRepository.findById(laneDTO.getRolProcesoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "RolProceso no encontrado con ID: " + laneDTO.getRolProcesoId()));

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
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + procesoId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return laneRepository.findByProcesoId(procesoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LaneDTO obtenerLanePorId(Long laneId) {
        Lane lane = obtenerLaneEntityById(laneId);
        return convertirADTO(lane);
    }

    @Override
    @Transactional(readOnly = true)
    public Lane obtenerLaneEntityById(Long laneId) {
        return laneRepository.findById(laneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lane no encontrado con ID: " + laneId));
    }

    @Override
    @Transactional(readOnly = true)
    public void validarLanePerteneceAlProceso(Long laneId, Long procesoId) {
        Lane lane = obtenerLaneEntityById(laneId);
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
        Lane lane = obtenerLaneEntityById(laneId);

        Proceso proceso = lane.getProceso();
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

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
        if (laneDTO.getRolProcesoId() != null) {
            RolProceso rolProceso = rolProcesoRepository.findById(laneDTO.getRolProcesoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "RolProceso no encontrado con ID: " + laneDTO.getRolProcesoId()));

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
        Lane lane = obtenerLaneEntityById(laneId);

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, lane.getProceso().getPool().getEmpresa());

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
        dto.setProcesoId(lane.getProceso() != null ? lane.getProceso().getId() : null);
        dto.setRolProcesoId(lane.getRolProceso() != null ? lane.getRolProceso().getId() : null);
        return dto;
    }

    private Usuario obtenerUsuarioActual() {
        String email = httpServletRequest.getHeader("X-User-Email");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("No se proporcionó el header X-User-Email para identificar al usuario");
        }
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        "Usuario no encontrado con el email: " + email));
    }

    private void validarUsuarioPertenecAEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario.getEmpresa() == null
                || !usuario.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException(
                    "El usuario no tiene acceso a la empresa con ID: " + empresa.getId());
        }
    }
}
