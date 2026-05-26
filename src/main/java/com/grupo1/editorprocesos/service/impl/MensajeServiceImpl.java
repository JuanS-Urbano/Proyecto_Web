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
import com.grupo1.editorprocesos.service.MensajeService;
import com.grupo1.editorprocesos.service.ProcesoService;
import com.grupo1.editorprocesos.service.UsuarioActualService;
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
    private final ProcesoService procesoService;
    private final ActividadRepository actividadRepository;
    private final UsuarioActualService usuarioActualService;

    // =====================================================================================
    // HU-25 (Dev 1): Message Throw
    // =====================================================================================

    @Override
    @Transactional
    public MensajeDTO throwMessage(MensajeDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del mensaje es requerido");
        }
        if (dto.getProceso() == null || dto.getProceso().getId() == null) {
            throw new IllegalArgumentException("La referencia al proceso es requerida");
        }

        Proceso proceso = procesoService.obtenerEntityById(dto.getProceso().getId());

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
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

        if (dto.getActividadOrigen() != null && dto.getActividadOrigen().getId() != null) {
            Actividad actividad = actividadRepository.findById(dto.getActividadOrigen().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Actividad origen no encontrada con ID: " + dto.getActividadOrigen().getId()));
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
        if (dto.getProcesoDestino() == null || dto.getProcesoDestino().getId() == null) {
            throw new IllegalArgumentException("La referencia al proceso destino es obligatoria para catchMessage");
        }

        // Verificar que el proceso destino exista
        Proceso procesoDestino = procesoService.obtenerEntityById(dto.getProcesoDestino().getId());

        // Verificar que no haya CATCH duplicado
        boolean yaExiste = mensajeRepository
                .existsByNombreAndTipoAndProcesoDestinoId(
                        dto.getNombre(), TipoMensaje.CATCH, dto.getProcesoDestino().getId());

        if (yaExiste) {
            throw new MensajeCatchException(
                    "Ya existe un mensaje CATCH con nombre '" + dto.getNombre()
                            + "' para el proceso con id " + dto.getProcesoDestino().getId());
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setNombre(dto.getNombre());
        mensaje.setPayloadJson(dto.getPayloadJson());
        mensaje.setTipo(TipoMensaje.CATCH);
        mensaje.setEstado(EstadoMensaje.PENDIENTE);
        mensaje.setCorrelationKey(dto.getCorrelationKey());
        mensaje.setProcesoDestinoId(dto.getProcesoDestino().getId());
        // Proceso origen para el catch (si se provee)
        if (dto.getProceso() != null && dto.getProceso().getId() != null) {
            Proceso procesoOrigen = procesoService.obtenerEntityById(dto.getProceso().getId());
            mensaje.setProceso(procesoOrigen);
        } else {
            mensaje.setProceso(procesoDestino);
        }

        log.info("[HU-27] catchMessage → nombre='{}', procesoDestino={}",
                dto.getNombre(), dto.getProcesoDestino().getId());

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
        Proceso proceso = procesoService.obtenerEntityById(procesoId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
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
        dto.setProceso(mensaje.getProceso() != null ? new com.grupo1.editorprocesos.dto.ReferenciaDTO(mensaje.getProceso().getId(), mensaje.getProceso().getNombre()) : null);
        dto.setActividadOrigen(mensaje.getActividadOrigen() != null
                ? new com.grupo1.editorprocesos.dto.ReferenciaDTO(mensaje.getActividadOrigen().getId(), mensaje.getActividadOrigen().getNombre()) : null);
        dto.setProcesoDestino(mensaje.getProcesoDestinoId() != null ? new com.grupo1.editorprocesos.dto.ReferenciaDTO(mensaje.getProcesoDestinoId(), null) : null);
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
