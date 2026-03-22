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
