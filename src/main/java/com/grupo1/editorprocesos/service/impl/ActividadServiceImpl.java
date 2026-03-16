package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.ActividadDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.ActividadService;
import com.grupo1.editorprocesos.service.LaneService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialCambiosRepository historialCambiosRepository;
    private final LaneService laneService;
    private final HttpServletRequest httpServletRequest;

    // =====================================================================================
    // HU-08: Crear Actividad
    // =====================================================================================

    @Override
    @Transactional
    public ActividadDTO crearActividad(ActividadDTO actividadDTO) {
        // 1. Validar que el proceso exista
        Proceso proceso = procesoRepository.findById(actividadDTO.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + actividadDTO.getProcesoId()));

        // 2. Validar que el usuario actual pertenezca a la empresa del proceso
        Usuario usuarioActual = obtenerUsuarioActual();
        Empresa empresa = proceso.getPool().getEmpresa();
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);

        // 3. Validar tipo de actividad
        if (actividadDTO.getTipoActividad() == null) {
            throw new IllegalArgumentException("El tipo de actividad es requerido");
        }

        // 4. Crear la entidad Actividad
        Actividad actividad = new Actividad();
        actividad.setNombre(actividadDTO.getNombre());
        actividad.setTipoActividad(actividadDTO.getTipoActividad());
        actividad.setPosicionX(actividadDTO.getPosicionX());
        actividad.setPosicionY(actividadDTO.getPosicionY());
        actividad.setProceso(proceso);

        // =====================================================================================
        // HU-22: Validar que el lane exista y pertenezca al mismo proceso que la actividad.
        // =====================================================================================
        if (actividadDTO.getLaneId() != null) {
            laneService.validarLanePerteneceAlProceso(actividadDTO.getLaneId(), proceso.getId());
            Lane lane = laneService.obtenerLaneEntityById(actividadDTO.getLaneId());
            actividad.setLane(lane);
        }

        // 5. Persistir
        Actividad guardada = actividadRepository.save(actividad);

        // 6. Registrar en historial
        registrarHistorial(proceso, usuarioActual,
                "Actividad creada: \"" + guardada.getNombre()
                        + "\" (Tipo: " + guardada.getTipoActividad() + ")");

        return convertirADTO(guardada);
    }

    // =====================================================================================
    // HU-09: Editar Actividad
    // =====================================================================================

    @Override
    @Transactional
    public ActividadDTO editarActividad(Long id, ActividadDTO actividadDTO) {
        // 1. Buscar actividad existente
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con ID: " + id));

        // 2. Validar pertenencia a empresa
        Proceso proceso = actividad.getProceso();
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        // 3. Rastrear cambios campo por campo
        StringBuilder cambios = new StringBuilder("Actividad editada (ID: " + id + "): ");
        boolean huboCambios = false;

        if (actividadDTO.getNombre() != null && !actividadDTO.getNombre().equals(actividad.getNombre())) {
            cambios.append("Nombre: \"").append(actividad.getNombre())
                    .append("\" → \"").append(actividadDTO.getNombre()).append("\". ");
            actividad.setNombre(actividadDTO.getNombre());
            huboCambios = true;
        }

        if (actividadDTO.getTipoActividad() != null && !actividadDTO.getTipoActividad().equals(actividad.getTipoActividad())) {
            cambios.append("Tipo: ").append(actividad.getTipoActividad())
                    .append(" → ").append(actividadDTO.getTipoActividad()).append(". ");
            actividad.setTipoActividad(actividadDTO.getTipoActividad());
            huboCambios = true;

            // =====================================================================================
            // TODO (Dev 4 — HU-11/HU-12): Al cambiar el tipo de actividad, considerar si
            // los arcos conectados a esta actividad siguen siendo válidos. Por ejemplo,
            // un cambio de RECEPCION a ENVIO podría requerir revalidar las conexiones.
            // Dev 4 debe implementar esta validación en ArcoService.
            // =====================================================================================
        }

        if (actividadDTO.getPosicionX() != null && !actividadDTO.getPosicionX().equals(actividad.getPosicionX())) {
            cambios.append("PosicionX: ").append(actividad.getPosicionX())
                    .append(" → ").append(actividadDTO.getPosicionX()).append(". ");
            actividad.setPosicionX(actividadDTO.getPosicionX());
            huboCambios = true;
        }

        if (actividadDTO.getPosicionY() != null && !actividadDTO.getPosicionY().equals(actividad.getPosicionY())) {
            cambios.append("PosicionY: ").append(actividad.getPosicionY())
                    .append(" → ").append(actividadDTO.getPosicionY()).append(". ");
            actividad.setPosicionY(actividadDTO.getPosicionY());
            huboCambios = true;
        }

        // =====================================================================================
        // TODO (Dev 2 — HU-22): Al cambiar el laneId, validar que el nuevo lane
        // pertenezca al mismo proceso. Dev 2 debe agregar esta validación cruzada.
        // =====================================================================================
        if (actividadDTO.getLaneId() != null) {
            Long laneActualId = actividad.getLane() != null ? actividad.getLane().getId() : null;
            if (!actividadDTO.getLaneId().equals(laneActualId)) {
                laneService.validarLanePerteneceAlProceso(actividadDTO.getLaneId(), proceso.getId());
                Lane nuevoLane = laneService.obtenerLaneEntityById(actividadDTO.getLaneId());
                cambios.append("Lane: ").append(laneActualId)
                        .append(" → ").append(actividadDTO.getLaneId()).append(". ");
                actividad.setLane(nuevoLane);
                huboCambios = true;
            }
        }

        // 4. Guardar cambios
        Actividad actualizada = actividadRepository.save(actividad);

        // 5. Registrar historial si hubo cambios
        if (huboCambios) {
            registrarHistorial(proceso, usuarioActual, cambios.toString());
        }

        return convertirADTO(actualizada);
    }

    // =====================================================================================
    // Consultas de lectura
    // =====================================================================================

    @Override
    @Transactional(readOnly = true)
    public ActividadDTO obtenerActividadPorId(Long id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con ID: " + id));
        return convertirADTO(actividad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadDTO> listarActividadesPorProceso(Long procesoId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + procesoId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return actividadRepository.findByProcesoId(procesoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ===== Métodos privados de utilidad =====

    private ActividadDTO convertirADTO(Actividad actividad) {
        ActividadDTO dto = new ActividadDTO();
        dto.setId(actividad.getId());
        dto.setNombre(actividad.getNombre());
        dto.setTipoActividad(actividad.getTipoActividad());
        dto.setPosicionX(actividad.getPosicionX());
        dto.setPosicionY(actividad.getPosicionY());
        dto.setProcesoId(actividad.getProceso().getId());
        if (actividad.getLane() != null) {
            dto.setLaneId(actividad.getLane().getId());
        }
        return dto;
    }

    private void registrarHistorial(Proceso proceso, Usuario usuario, String cambio) {
        HistorialCambios historial = new HistorialCambios();
        historial.setProceso(proceso);
        historial.setUsuarioId(usuario.getId());
        historial.setFecha(LocalDateTime.now());
        historial.setCambio(cambio);
        historialCambiosRepository.save(historial);
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
