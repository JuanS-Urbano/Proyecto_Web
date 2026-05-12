package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.CorrelacionResultDTO;
import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.service.MensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;

    /**
     * HU-25 (Dev 1): Enviar un Message Throw.
     */
    @PostMapping("/throw")
    public ResponseEntity<ApiResponse<MensajeDTO>> throwMessage(@Valid @RequestBody MensajeDTO dto) {
        MensajeDTO creado = mensajeService.throwMessage(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Mensaje Throw enviado exitosamente", creado));
    }

    /**
     * HU-27 (Dev 2): Capturar un Message Catch.
     */
    @PostMapping("/catch")
    public ResponseEntity<ApiResponse<MensajeDTO>> catchMessage(@Valid @RequestBody MensajeDTO dto) {
        MensajeDTO resultado = mensajeService.catchMessage(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Mensaje CATCH registrado exitosamente", resultado));
    }

    /**
     * Listar mensajes de un proceso.
     */
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<ApiResponse<List<MensajeDTO>>> listarPorProceso(
            @PathVariable Long procesoId) {
        List<MensajeDTO> mensajes = mensajeService.listarMensajesPorProceso(procesoId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mensajes obtenidos exitosamente", mensajes));
    }

    /**
     * Obtener un mensaje por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MensajeDTO>> obtenerPorId(@PathVariable Long id) {
        MensajeDTO mensaje = mensajeService.obtenerMensajePorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mensaje obtenido exitosamente", mensaje));
    }

    // =====================================================================================
    // HU-28 (Dev 3): Correlación de Mensajes
    // =====================================================================================

    /**
     * HU-28: Ejecuta la correlación THROW↔CATCH por correlationKey.
     */
    @PostMapping("/correlate/{correlationKey}")
    public ResponseEntity<ApiResponse<CorrelacionResultDTO>> correlateMessages(
            @PathVariable String correlationKey) {
        CorrelacionResultDTO resultado = mensajeService.correlateMessages(correlationKey);
        return ResponseEntity.ok(new ApiResponse<>(true, resultado.getMensaje(), resultado));
    }

    /**
     * HU-28: Lista todos los mensajes con un correlationKey dado.
     */
    @GetMapping("/correlation/{correlationKey}")
    public ResponseEntity<ApiResponse<List<MensajeDTO>>> buscarPorCorrelationKey(
            @PathVariable String correlationKey) {
        List<MensajeDTO> mensajes = mensajeService.buscarPorCorrelationKey(correlationKey);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mensajes por correlationKey obtenidos", mensajes));
    }
}
