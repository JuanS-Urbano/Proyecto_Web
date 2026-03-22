package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.MensajeDTO;
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

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final ProcesoRepository procesoRepository;
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
