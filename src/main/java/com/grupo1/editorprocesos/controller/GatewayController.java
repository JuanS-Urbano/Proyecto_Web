package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.GatewayDTO;
import com.grupo1.editorprocesos.service.GatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gateways")
@RequiredArgsConstructor
public class GatewayController {

    private final GatewayService gatewayService;

    /**
     * HU-15: Crear un nuevo gateway dentro de un proceso.
     * Body requerido: nombre, tipoGateway, procesoId.
     * Opcionales: posicionX, posicionY.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GatewayDTO>> crearGateway(@Valid @RequestBody GatewayDTO gatewayDTO) {
        GatewayDTO creado = gatewayService.crearGateway(gatewayDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Gateway creado exitosamente", creado));
    }

    /**
     * HU-15: Editar un gateway existente.
     * Solo se actualizan los campos enviados (parcial update).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GatewayDTO>> editarGateway(
            @PathVariable Long id,
            @Valid @RequestBody GatewayDTO gatewayDTO) {
        GatewayDTO actualizado = gatewayService.editarGateway(id, gatewayDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Gateway actualizado exitosamente", actualizado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GatewayDTO>> obtenerGateway(@PathVariable Long id) {
        GatewayDTO gateway = gatewayService.obtenerGatewayPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Gateway obtenido exitosamente", gateway));
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<ApiResponse<List<GatewayDTO>>> listarGatewaysPorProceso(@PathVariable Long procesoId) {
        List<GatewayDTO> gateways = gatewayService.listarGatewaysPorProceso(procesoId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Gateways obtenidos exitosamente", gateways));
    }

    /**
     * HU-16: Eliminar un gateway con saneamiento del grafo.
     * Requiere el parámetro ?confirmar=true para proceder con la eliminación.
     * Sin confirmación devuelve HTTP 400 para evitar eliminaciones accidentales.
     * La lógica de reconexión/huérfanos es ejecutada dentro del servicio.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<ApiResponse<Void>> eliminarGateway(
            @PathVariable Long id,
            @RequestParam(name = "confirmar", defaultValue = "false") boolean confirmar) {

        if (!confirmar) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false,
                            "Se requiere confirmación explícita para eliminar un gateway. "
                                    + "Envíe ?confirmar=true para proceder.", null));
        }

        gatewayService.eliminarGateway(id);
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Gateway eliminado exitosamente. Los arcos conectados fueron saneados.", null));
    }
}
