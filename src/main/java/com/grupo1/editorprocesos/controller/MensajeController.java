package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para mensajería BPMN (HU-27 — Message Catch).
 *
 * ─── Cruce Throw/Catch con Dev 1 ──────────────────────────────────────────
 * Dev 1 puede probar el flujo completo sin tener throwMessage() listo:
 *
 *   1. Crear el proceso destino (ya existe vía ProcesoController):
 *      POST /api/v1/procesos
 *
 *   2. Registrar el CATCH (este endpoint):
 *      POST /api/v1/mensajes/catch
 *      Body: {
 *        "nombre": "msgPagoAprobado",
 *        "tipo": "CATCH",
 *        "procesoOrigenId": 1,
 *        "procesoDestinoId": 2,
 *        "payloadJson": null
 *      }
 *
 *   3. Verificar el CATCH registrado:
 *      GET /api/v1/mensajes/{id}
 *      GET /api/v1/mensajes/proceso/{procesoId}
 *
 * Cuando Dev 1 implemente throwMessage(), simplemente llamará
 * POST /api/v1/mensajes/throw con el mismo 'nombre' y el flujo quedará cruzado.
 * ─────────────────────────────────────────────────────────────────────────
 */
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
    }
}
