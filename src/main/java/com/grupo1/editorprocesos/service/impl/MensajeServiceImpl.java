package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.CorrelacionResultDTO;
import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.exception.MensajeCatchException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S125")
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final ProcesoRepository procesoRepository;
    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final HttpServletRequest httpServletRequest;

    // =====================================================================================
    // HU-25 (Dev 1): Message Throw
    // =====================================================================================

    @Override
    @Transactional
    public MensajeDTO throwMessage(MensajeDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del mensaje es requerido");
        }
        if (dto.getProcesoId() == null) {
            throw new IllegalArgumentException("El procesoId es requerido");
        }

        Proceso proceso = procesoRepository.findById(dto.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + dto.getProcesoId()));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        if (dto.getPayloadJson() != null && !dto.getPayloadJson().isBlank()) {
            validarJson(dto.getPayloadJson());
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setNombre(dto.getNombre());
        mensaje.setPayloadJson(dto.getPayloadJson());
        mensaje.setTipo(TipoMensaje.THROW);
        mensaje.setEstado(EstadoMensaje.PENDIENTE);
        mensaje.setCorrelationKey(dto.getCorrelationKey());
        mensaje.setProceso(proceso);

        if (dto.getActividadOrigenId() != null) {
            Actividad actividad = actividadRepository.findById(dto.getActividadOrigenId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Actividad origen no encontrada con ID: " + dto.getActividadOrigenId()));
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
    // HU-27 (Dev 2): Message Catch
    // =====================================================================================

    @Override
    @Transactional
    public MensajeDTO catchMessage(MensajeDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del mensaje BPMN es obligatorio");
        }
        if (dto.getProcesoDestinoId() == null) {
            throw new IllegalArgumentException("El ID del proceso destino es obligatorio para catchMessage");
        }

        // Verificar que el proceso destino exista
        Proceso procesoDestino = procesoRepository.findById(dto.getProcesoDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso destino no encontrado con id: " + dto.getProcesoDestinoId()));

        // Verificar que no haya CATCH duplicado
        boolean yaExiste = mensajeRepository
                .existsByNombreAndTipoAndProcesoDestinoId(
                        dto.getNombre(), TipoMensaje.CATCH, dto.getProcesoDestinoId());

        if (yaExiste) {
            throw new MensajeCatchException(
                    "Ya existe un mensaje CATCH con nombre '" + dto.getNombre()
                            + "' para el proceso con id " + dto.getProcesoDestinoId());
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setNombre(dto.getNombre());
        mensaje.setPayloadJson(dto.getPayloadJson());
        mensaje.setTipo(TipoMensaje.CATCH);
        mensaje.setEstado(EstadoMensaje.PENDIENTE);
        mensaje.setCorrelationKey(dto.getCorrelationKey());
        mensaje.setProcesoDestinoId(dto.getProcesoDestinoId());
        // Proceso origen para el catch (si se provee)
        if (dto.getProcesoId() != null) {
            Proceso procesoOrigen = procesoRepository.findById(dto.getProcesoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Proceso origen no encontrado con ID: " + dto.getProcesoId()));
            mensaje.setProceso(procesoOrigen);
        } else {
            mensaje.setProceso(procesoDestino);
        }

        log.info("[HU-27] catchMessage → nombre='{}', procesoDestino={}",
                dto.getNombre(), dto.getProcesoDestinoId());

        Mensaje guardado = mensajeRepository.save(mensaje);

        log.info("[HU-27] Mensaje CATCH persistido → id={}", guardado.getId());

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
                .toList();
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
    // HU-28 (Dev 3): Correlación de Mensajes
    // =====================================================================================

    @Override
    @Transactional
    public CorrelacionResultDTO correlateMessages(String correlationKey) {
        // Validar correlationKey
        if (correlationKey == null || correlationKey.isBlank()) {
            throw new IllegalArgumentException("El correlationKey es requerido para la correlación");
        }

        log.info("[HU-28] correlateMessages → key='{}'", correlationKey);

        // Buscar THROW PENDIENTE con esta key
        Optional<Mensaje> throwOpt = mensajeRepository
                .findFirstByCorrelationKeyAndTipoAndEstado(
                        correlationKey, TipoMensaje.THROW, EstadoMensaje.PENDIENTE);

        if (throwOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No se encontró un Message THROW pendiente con correlationKey: " + correlationKey);
        }

        // Buscar CATCH PENDIENTE con esta key
        Optional<Mensaje> catchOpt = mensajeRepository
                .findFirstByCorrelationKeyAndTipoAndEstado(
                        correlationKey, TipoMensaje.CATCH, EstadoMensaje.PENDIENTE);

        if (catchOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No se encontró un Message CATCH pendiente con correlationKey: " + correlationKey);
        }

        Mensaje throwMsg = throwOpt.get();
        Mensaje catchMsg = catchOpt.get();

        // Copiar payload del THROW al CATCH (si existe)
        if (throwMsg.getPayloadJson() != null && !throwMsg.getPayloadJson().isBlank()) {
            catchMsg.setPayloadJson(throwMsg.getPayloadJson());
        }

        // Marcar ambos como ENTREGADO
        throwMsg.setEstado(EstadoMensaje.ENTREGADO);
        catchMsg.setEstado(EstadoMensaje.ENTREGADO);

        mensajeRepository.save(throwMsg);
        mensajeRepository.save(catchMsg);

        log.info("[HU-28] Correlación exitosa → THROW id={}, CATCH id={}, key='{}'",
                throwMsg.getId(), catchMsg.getId(), correlationKey);

        return new CorrelacionResultDTO(
                convertirADTO(throwMsg),
                convertirADTO(catchMsg),
                true,
                "Correlación exitosa para key: " + correlationKey
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDTO> buscarPorCorrelationKey(String correlationKey) {
        if (correlationKey == null || correlationKey.isBlank()) {
            throw new IllegalArgumentException("El correlationKey es requerido");
        }
        return mensajeRepository.findByCorrelationKey(correlationKey).stream()
                .map(this::convertirADTO)
                .toList();
    }

    // =====================================================================================
    // Métodos privados
    // =====================================================================================

    @SuppressWarnings({"java:S3776", "java:S135", "java:S125"})
    private void validarJson(String json) {
        String trimmed = json.trim();
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))
                && !(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            throw new IllegalArgumentException(
                    "El payload no es un JSON válido: debe comenzar con '{' o '['");
        }

        int braces = 0;
        int brackets = 0;
        boolean inString = false;
        boolean escaped = false;

        for (char c : trimmed.toCharArray()) {
            if (escaped) { escaped = false; continue; }
            if (c == '\\') { escaped = true; continue; }
            if (c == '"') { inString = !inString; continue; }
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
        dto.setProcesoDestinoId(mensaje.getProcesoDestinoId());
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
