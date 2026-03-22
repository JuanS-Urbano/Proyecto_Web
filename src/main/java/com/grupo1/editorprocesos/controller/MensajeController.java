package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.service.MensajeService;
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
     * HU-27: Captura un mensaje BPMN e integra al flujo del proceso destino.
     */
    @PostMapping("/catch")
    public ResponseEntity<ApiResponse<MensajeDTO>> catchMessage(@RequestBody MensajeDTO mensajeDTO) {
        MensajeDTO resultado = mensajeService.catchMessage(mensajeDTO);
        ApiResponse<MensajeDTO> response = new ApiResponse<>(
                true, "Mensaje CATCH registrado exitosamente", resultado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene un mensaje por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MensajeDTO>> obtenerMensaje(@PathVariable Long id) {
        MensajeDTO mensaje = mensajeService.obtenerMensajePorId(id);
        ApiResponse<MensajeDTO> response = new ApiResponse<>(
                true, "Mensaje obtenido exitosamente", mensaje);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los mensajes CATCH registrados para un proceso destino.
     */
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<ApiResponse<List<MensajeDTO>>> listarMensajesPorProceso(
            @PathVariable Long procesoId) {
        List<MensajeDTO> mensajes = mensajeService.listarMensajesPorProceso(procesoId);
        ApiResponse<List<MensajeDTO>> response = new ApiResponse<>(
                true, "Mensajes del proceso obtenidos exitosamente", mensajes);
        return ResponseEntity.ok(response);
    }

    /**
     * Stub para throwMessage — Dev 1 (HU-25).
     * Declarado aquí para documentar el contrato REST del cruce Throw/Catch.
     * Retorna 501 hasta que Dev 1 implemente el método en el servicio.
     */
    @PostMapping("/throw")
    public ResponseEntity<ApiResponse<Void>> throwMessage(@RequestBody MensajeDTO mensajeDTO) {
        ApiResponse<Void> response = new ApiResponse<>(
                false, "throwMessage() pendiente de implementación por Dev 1 (HU-25)", null);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
     * HU-25: Enviar un Message Throw.
     */
    @PostMapping("/throw")
    public ResponseEntity<ApiResponse<MensajeDTO>> throwMessage(@RequestBody MensajeDTO dto) {
        MensajeDTO creado = mensajeService.throwMessage(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Mensaje Throw enviado exitosamente", creado));
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
}
