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
}
