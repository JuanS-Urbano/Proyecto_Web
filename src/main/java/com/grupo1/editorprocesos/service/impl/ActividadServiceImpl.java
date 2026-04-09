package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.ActividadDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.TipoActividad;
import com.grupo1.editorprocesos.repository.ArcoRepository;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.service.LaneService;
import com.grupo1.editorprocesos.repository.LaneRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.ActividadService;
import com.grupo1.editorprocesos.service.PermisosPoolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings({"java:S1192", "java:S1172", "java:S1481", "java:S1854", "java:S1128"})
@Service
@RequiredArgsConstructor
public class ActividadServiceImpl implements ActividadService {

    private static final String ACTIVIDAD_NO_ENCONTRADA = "No se encontro la actividad con ID: ";

    private final ActividadRepository actividadRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialCambiosRepository historialCambiosRepository;
    private final ArcoRepository arcoRepository;
    private final LaneService laneService;
    private final LaneRepository laneRepository;
    private final HttpServletRequest httpServletRequest;
    private final PermisosPoolService permisosPoolService;

    // =====================================================================================
    // HU-08: Crear Actividad
    // =====================================================================================

    @Override
    @Transactional
    public ActividadDTO crearActividad(ActividadDTO actividadDTO) {
        // 1. Validar que el proceso exista
        if (actividadDTO.getProceso() == null || actividadDTO.getProceso().getId() == null) {
            throw new IllegalArgumentException("La referencia al proceso es requerida");
        }

        Proceso proceso = procesoRepository.findById(actividadDTO.getProceso().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + actividadDTO.getProceso().getId()));

        // 2. Validar que el usuario actual pertenezca a la empresa del proceso
        Usuario usuarioActual = obtenerUsuarioActual();
        Empresa empresa = proceso.getPool().getEmpresa();
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);
        permisosPoolService.validarPermisoEscritura(usuarioActual);

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
        if (actividadDTO.getLane() != null && actividadDTO.getLane().getId() != null) {
            laneService.validarLanePerteneceAlProceso(actividadDTO.getLane().getId(), proceso.getId());
            Lane lane = laneService.obtenerLaneEntityById(actividadDTO.getLane().getId());
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
                        ACTIVIDAD_NO_ENCONTRADA + id));

        // 2. Validar pertenencia a empresa
        Proceso proceso = actividad.getProceso();
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());
        permisosPoolService.validarPermisoEscritura(usuarioActual);

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

        // HU-22: Al cambiar el laneId, validar que el nuevo lane pertenezca al mismo proceso.
        if (actividadDTO.getLane() != null && actividadDTO.getLane().getId() != null) {
            Long laneActualId = actividad.getLane() != null ? actividad.getLane().getId() : null;
            if (!actividadDTO.getLane().getId().equals(laneActualId)) {
                laneService.validarLanePerteneceAlProceso(actividadDTO.getLane().getId(), proceso.getId());
                Lane nuevoLane = laneService.obtenerLaneEntityById(actividadDTO.getLane().getId());
                cambios.append("Lane: ").append(laneActualId)
                        .append(" → ").append(actividadDTO.getLane().getId()).append(". ");
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
                        ACTIVIDAD_NO_ENCONTRADA + id));
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
                .toList();
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
                        ACTIVIDAD_NO_ENCONTRADA + id));

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
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(actividad.getProceso().getId(), actividad.getProceso().getNombre()));
        if (actividad.getLane() != null) {
            dto.setLane(new com.grupo1.editorprocesos.dto.ReferenciaDTO(actividad.getLane().getId(), actividad.getLane().getNombre()));
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
     * En futuras versiones se pueden agregar reglas BPMN específicas aquí.
     */
    @SuppressWarnings("java:S1172") // Parámetros serán usados en futuras validaciones BPMN
    private void validarArcosConectadosAlCambiarTipo(Actividad actividad, TipoActividad nuevoTipo) {
        // Placeholder: en futuras versiones se validarán reglas BPMN específicas.
    }
}
