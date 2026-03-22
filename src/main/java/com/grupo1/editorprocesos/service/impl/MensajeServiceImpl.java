package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.exception.MensajeCatchException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.repository.MensajeRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.service.MensajeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.MensajeRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.MensajeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de mensajería.
 * HU-27 (Dev 2): catchMessage() — captura y persiste un mensaje BPMN entrante.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * GUÍA DE CRUCE THROW/CATCH CON DEV 1
 * ───────────────────────────────────────────────────────────────────────────
 * Flujo completo cuando ambos devs integren:
 *   1. Dev 1 llama throwMessage(dto) → persiste Mensaje con tipo="THROW"
 *   2. Dev 1 llama catchMessage(dto) → este método recibe y persiste tipo="CATCH"
 *   3. Ambos mensajes quedan vinculados por el mismo 'nombre' de mensaje BPMN.
 *
 * Prueba rápida sin throwMessage listo (Dev 1 puede testear directamente):
 *   POST /api/v1/mensajes/catch
 *   Body: { "nombre":"msgPagoAprobado",
 *            "tipo":"CATCH",
 *            "procesoOrigenId":1,
 *            "procesoDestinoId":2,
 *            "payloadJson":null }
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final ProcesoRepository procesoRepository;

    // ══════════════════════════════════════════════════════════════════════
    // HU-27 — catchMessage()
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Captura un mensaje BPMN entrante, lo valida y lo persiste en la base de datos
     * con tipo "CATCH", integrándolo al flujo del proceso destino.
     *
     * Pasos internos:
     *  1. Validar DTO (nombre obligatorio, proceso destino existente).
     *  2. Verificar que el proceso destino esté activo/exista.
     *  3. Verificar que no exista ya un CATCH duplicado para el mismo mensaje/proceso.
     *  4. Persistir la entidad Mensaje con tipo="CATCH".
     *  5. Retornar el DTO con el ID generado.
     *
     * @param mensajeDTO datos del mensaje entrante; nombre y procesoDestinoId obligatorios
     * @return DTO del mensaje persistido con tipo CATCH
     * @throws IllegalArgumentException si faltan campos obligatorios
     * @throws ResourceNotFoundException si el proceso destino no existe
     * @throws MensajeCatchException si ya existe un CATCH activo para ese mensaje/proceso
     */
    @Override
    @Transactional
    public MensajeDTO catchMessage(MensajeDTO mensajeDTO) {

        // ── 1. Validación de entrada ───────────────────────────────────────
        validarDtoCatch(mensajeDTO);

        log.info("[HU-27] catchMessage → nombre='{}', procesoDestino={}",
                mensajeDTO.getNombre(), mensajeDTO.getProcesoDestinoId());

        // ── 2. Verificar que el proceso destino exista ─────────────────────
        Proceso procesoDestino = procesoRepository.findById(mensajeDTO.getProcesoDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso destino no encontrado con id: " + mensajeDTO.getProcesoDestinoId()));

        // ── 3. Verificar que no haya CATCH duplicado para este mensaje/proceso
        boolean yaExiste = mensajeRepository
                .existsByNombreAndTipoAndProcesoDestinoId(
                        mensajeDTO.getNombre(), "CATCH", mensajeDTO.getProcesoDestinoId());

        if (yaExiste) {
            throw new MensajeCatchException(
                    "Ya existe un mensaje CATCH con nombre '" + mensajeDTO.getNombre() +
                    "' para el proceso con id " + mensajeDTO.getProcesoDestinoId());
        }

        // ── 4. Construir y persistir la entidad ────────────────────────────
        Mensaje mensaje = toEntity(mensajeDTO);
        mensaje.setTipo("CATCH");

        Mensaje guardado = mensajeRepository.save(mensaje);

        log.info("[HU-27] Mensaje CATCH persistido → id={}, nombre='{}', procesoDestino={}",
                guardado.getId(), guardado.getNombre(), mensajeDTO.getProcesoDestinoId());

        // ── 5. Retornar DTO ────────────────────────────────────────────────
        return toDTO(guardado, mensajeDTO.getProcesoOrigenId(), mensajeDTO.getProcesoDestinoId());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Consultas
    // ══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public MensajeDTO obtenerMensajePorId(Long id) {
        Mensaje mensaje = mensajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con id: " + id));
        return toDTO(mensaje, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDTO> listarMensajesPorProceso(Long procesoId) {
        return mensajeRepository.findByProcesoDestinoId(procesoId)
                .stream()
                .map(m -> toDTO(m, null, procesoId))
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════
    // throwMessage() — STUB: implementación pendiente de Dev 1 (HU-25)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * @throws UnsupportedOperationException siempre — implementación de Dev 1 (HU-25)
     */
    @Override
    public MensajeDTO throwMessage(MensajeDTO mensajeDTO) {
        throw new UnsupportedOperationException(
                "throwMessage() es responsabilidad de Dev 1 (HU-25). No implementar aquí.");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Métodos auxiliares privados
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Valida los campos mínimos requeridos por catchMessage().
     * La correlación avanzada (businessKey, variables complejas) la valida Dev 3 (HU-28).
     */
    private void validarDtoCatch(MensajeDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo del mensaje no puede ser null");
        }
        if (!StringUtils.hasText(dto.getNombre())) {
            throw new IllegalArgumentException("El nombre del mensaje BPMN es obligatorio");
        }
        if (dto.getProcesoDestinoId() == null) {
            throw new IllegalArgumentException("El ID del proceso destino es obligatorio para catchMessage");
        }
    }

    /** Convierte DTO → entidad para persistencia. */
    private Mensaje toEntity(MensajeDTO dto) {
        Mensaje m = new Mensaje();
        m.setNombre(dto.getNombre());
        m.setPayloadJson(dto.getPayloadJson());
        m.setTipo(dto.getTipo() != null ? dto.getTipo() : "CATCH");
        return m;
    }

    /** Convierte entidad → DTO para respuesta. */
    private MensajeDTO toDTO(Mensaje m, Long procesoOrigenId, Long procesoDestinoId) {
        MensajeDTO dto = new MensajeDTO();
        dto.setId(m.getId());
        dto.setNombre(m.getNombre());
        dto.setPayloadJson(m.getPayloadJson());
        dto.setTipo(m.getTipo());
        dto.setProcesoOrigenId(procesoOrigenId);
        dto.setProcesoDestinoId(procesoDestinoId);
        return dto;
    }
    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final HttpServletRequest httpServletRequest;

    // =====================================================================================
    // HU-25: Message Throw
    // =====================================================================================

    @Override
    @Transactional
    public MensajeDTO throwMessage(MensajeDTO dto) {
        // Validar nombre
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del mensaje es requerido");
        }

        // Validar proceso
        if (dto.getProcesoId() == null) {
            throw new IllegalArgumentException("El procesoId es requerido");
        }

        Proceso proceso = procesoRepository.findById(dto.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + dto.getProcesoId()));

        // Validar multi-tenant
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        // Validar payload JSON (si se proporciona)
        if (dto.getPayloadJson() != null && !dto.getPayloadJson().isBlank()) {
            validarJson(dto.getPayloadJson());
        }

        // Construir entidad
        Mensaje mensaje = new Mensaje();
        mensaje.setNombre(dto.getNombre());
        mensaje.setPayloadJson(dto.getPayloadJson());
        mensaje.setTipo(TipoMensaje.THROW);
        mensaje.setEstado(EstadoMensaje.PENDIENTE);
        mensaje.setCorrelationKey(dto.getCorrelationKey());
        mensaje.setProceso(proceso);

        // Asociar actividad origen (opcional)
        if (dto.getActividadOrigenId() != null) {
            Actividad actividad = actividadRepository.findById(dto.getActividadOrigenId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Actividad origen no encontrada con ID: " + dto.getActividadOrigenId()));

            // Validar que la actividad pertenezca al mismo proceso
            if (!actividad.getProceso().getId().equals(proceso.getId())) {
                throw new IllegalArgumentException(
                        "La actividad origen no pertenece al proceso con ID: " + proceso.getId());
            }
            mensaje.setActividadOrigen(actividad);
        }

        Mensaje guardado = mensajeRepository.save(mensaje);
        return convertirADTO(guardado);
    }

    // =====================================================================================
    // Listar y obtener
    // =====================================================================================

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDTO> listarMensajesPorProceso(Long procesoId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + procesoId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return mensajeRepository.findByProcesoId(procesoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MensajeDTO obtenerMensajePorId(Long id) {
        Mensaje mensaje = mensajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + id));
        return convertirADTO(mensaje);
    }

    // =====================================================================================
    // Métodos privados
    // =====================================================================================

    private void validarJson(String json) {
        String trimmed = json.trim();
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))
                && !(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            throw new IllegalArgumentException(
                    "El payload no es un JSON válido: debe comenzar con '{' o '['");
        }

        // Verificar balance de llaves/corchetes
        int braces = 0;
        int brackets = 0;
        boolean inString = false;
        boolean escaped = false;

        for (char c : trimmed.toCharArray()) {
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '{') braces++;
                else if (c == '}') braces--;
                else if (c == '[') brackets++;
                else if (c == ']') brackets--;
            }
        }

        if (braces != 0 || brackets != 0) {
            throw new IllegalArgumentException(
                    "El payload no es un JSON válido: llaves o corchetes desbalanceados");
        }
    }

    private MensajeDTO convertirADTO(Mensaje mensaje) {
        MensajeDTO dto = new MensajeDTO();
        dto.setId(mensaje.getId());
        dto.setNombre(mensaje.getNombre());
        dto.setPayloadJson(mensaje.getPayloadJson());
        dto.setTipo(mensaje.getTipo());
        dto.setEstado(mensaje.getEstado());
        dto.setCorrelationKey(mensaje.getCorrelationKey());
        dto.setProcesoId(mensaje.getProceso() != null ? mensaje.getProceso().getId() : null);
        dto.setActividadOrigenId(mensaje.getActividadOrigen() != null
                ? mensaje.getActividadOrigen().getId() : null);
        return dto;
    }

    private Usuario obtenerUsuarioActual() {
        String email = httpServletRequest.getHeader("X-User-Email");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException(
                    "No se proporcionó el header X-User-Email para identificar al usuario");
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
