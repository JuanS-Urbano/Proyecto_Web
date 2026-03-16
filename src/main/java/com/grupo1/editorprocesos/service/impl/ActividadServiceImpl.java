package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.ActividadDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.TipoActividad;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.ArcoRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.repository.LaneRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.ActividadService;
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
    private final LaneRepository laneRepository;
    private final ArcoRepository arcoRepository;
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
        // TODO (Dev 2 — HU-22): Cuando LaneService esté implementado, se debe validar que:
        //   1. El laneId pertenezca al MISMO proceso que la actividad.
        //   2. El lane exista y esté activo.
        // Actualmente se asigna el lane si se proporciona el laneId, pero sin validación
        // cruzada con el proceso. Dev 2 debe agregar esa validación aquí.
        // =====================================================================================
        if (actividadDTO.getLaneId() != null) {
            Lane lane = laneRepository.findById(actividadDTO.getLaneId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Lane no encontrado con ID: " + actividadDTO.getLaneId()));
            // TODO (Dev 2): Validar que lane.getProceso().getId().equals(proceso.getId())
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
            // Validar arcos conectados antes de cambiar el tipo
            validarArcosConectadosAlCambiarTipo(actividad, actividadDTO.getTipoActividad());

            cambios.append("Tipo: ").append(actividad.getTipoActividad())
                    .append(" → ").append(actividadDTO.getTipoActividad()).append(". ");
            actividad.setTipoActividad(actividadDTO.getTipoActividad());
            huboCambios = true;
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
                Lane nuevoLane = laneRepository.findById(actividadDTO.getLaneId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Lane no encontrado con ID: " + actividadDTO.getLaneId()));
                // TODO (Dev 2): Validar que nuevoLane.getProceso().getId().equals(proceso.getId())
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

    // =====================================================================================
    // HU (DEV5): Eliminar Actividad — con saneamiento del grafo
    // =====================================================================================

    @Override
    @Transactional
    public void eliminarActividad(Long id) {
        // 1. Validar existencia de la actividad
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con ID: " + id));

        // 2. Validar usuario y empresa
        Proceso proceso = actividad.getProceso();
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        String nombreActividad = actividad.getNombre();
        Long procesoId = proceso.getId();

        // 3. Buscar arcos conectados dentro del mismo proceso
        List<Arco> arcosEntrantes = arcoRepository.findByDestinoIdAndProcesoId(nombreActividad, procesoId);
        List<Arco> arcosSalientes = arcoRepository.findByOrigenIdAndProcesoId(nombreActividad, procesoId);

        StringBuilder detalleHistorial = new StringBuilder(
                "Actividad eliminada: \"" + nombreActividad
                        + "\" (Tipo: " + actividad.getTipoActividad() + "). ");

        // 4. Aplicar regla de saneamiento del grafo
        // Regla: si exactamente 1 entrante y 1 saliente → reconectar
        // Si hay múltiples o ninguno → eliminar todos sin reconectar (dejar huérfanos)
        if (arcosEntrantes.size() == 1 && arcosSalientes.size() == 1) {
            String origenPredecesor = arcosEntrantes.get(0).getOrigenId();
            String destinoSucesor = arcosSalientes.get(0).getDestinoId();

            // Eliminar los dos arcos conectados a la actividad
            arcoRepository.delete(arcosEntrantes.get(0));
            arcoRepository.delete(arcosSalientes.get(0));

            // Verificar que no exista ya ese arco para evitar duplicado
            boolean yaExiste = arcoRepository
                    .findByOrigenIdAndDestinoIdAndProcesoId(origenPredecesor, destinoSucesor, procesoId)
                    .isPresent();

            if (!yaExiste) {
                Arco nuevoArco = new Arco();
                nuevoArco.setOrigenId(origenPredecesor);
                nuevoArco.setDestinoId(destinoSucesor);
                nuevoArco.setProceso(proceso);
                arcoRepository.save(nuevoArco);
                detalleHistorial.append("Arcos saneados: 2 eliminados, reconexión creada \"")
                        .append(origenPredecesor).append("\" → \"").append(destinoSucesor).append("\".");
            } else {
                detalleHistorial.append("Arcos saneados: 2 eliminados. Reconexión omitida (arco ya existe).");
            }
        } else {
            // Múltiples entrantes/salientes: eliminar todos sin reconectar
            int totalArcos = arcosEntrantes.size() + arcosSalientes.size();
            arcoRepository.deleteAll(arcosEntrantes);
            arcoRepository.deleteAll(arcosSalientes);
            if (totalArcos > 0) {
                detalleHistorial.append("Arcos huérfanos: ").append(totalArcos)
                        .append(" arcos conectados eliminados sin reconexión.");
            } else {
                detalleHistorial.append("Sin arcos conectados.");
            }
        }

        // 5. Eliminar la actividad
        actividadRepository.delete(actividad);

        // 6. Registrar en historial
        registrarHistorial(proceso, usuarioActual, detalleHistorial.toString());
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

    /**
     * Valida que al cambiar el tipo de actividad, los arcos conectados sigan siendo válidos.
     * Por ahora registra advertencia; en futuras versiones se puede lanzar excepción.
     */
    private void validarArcosConectadosAlCambiarTipo(Actividad actividad, TipoActividad nuevoTipo) {
        List<Arco> arcosEntrantes = arcoRepository.findByDestinoId(actividad.getNombre());
        List<Arco> arcosSalientes = arcoRepository.findByOrigenId(actividad.getNombre());
        // En el futuro se pueden validar reglas BPMN específicas aquí.
    }
}
