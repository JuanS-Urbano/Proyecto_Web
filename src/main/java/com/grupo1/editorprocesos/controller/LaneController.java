package com.grupo1.editorprocesos.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RestController;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.LaneDTO;
import com.grupo1.editorprocesos.service.LaneService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LaneController {

    private final LaneService laneService;

    @PostMapping("/procesos/{procesoId}/lanes")
    public ResponseEntity<ApiResponse<LaneDTO>> crearLane(
            @PathVariable Long procesoId,
            @Valid @RequestBody LaneDTO laneDTO) {
        LaneDTO creado = laneService.crearLane(procesoId, laneDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Lane creado exitosamente", creado));
    }

    @GetMapping("/procesos/{procesoId}/lanes")
    public ResponseEntity<ApiResponse<List<LaneDTO>>> listarLanesPorProceso(@PathVariable Long procesoId) {
        List<LaneDTO> lanes = laneService.listarLanesPorProceso(procesoId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lanes obtenidos exitosamente", lanes));
    }

    @GetMapping("/lanes/{laneId}")
    public ResponseEntity<ApiResponse<LaneDTO>> obtenerLane(@PathVariable Long laneId) {
        LaneDTO lane = laneService.obtenerLanePorId(laneId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lane obtenido exitosamente", lane));
    }

    @PutMapping("/lanes/{laneId}")
    public ResponseEntity<ApiResponse<LaneDTO>> editarLane(
            @PathVariable Long laneId,
            @Valid @RequestBody LaneDTO laneDTO) {
        LaneDTO actualizado = laneService.editarLane(laneId, laneDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lane actualizado exitosamente", actualizado));
    }

    /**
     * Eliminar un lane. Valida que no tenga actividades asignadas.
     * Requiere ?confirmar=true para proceder.
     */
    @DeleteMapping("/lanes/{laneId}")
    public ResponseEntity<ApiResponse<Void>> eliminarLane(
            @PathVariable Long laneId,
            @RequestParam(name = "confirmar", defaultValue = "false") boolean confirmar) {

        if (!confirmar) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false,
                            "Se requiere confirmación explícita. Envíe ?confirmar=true para eliminar.", null));
        }

        laneService.eliminarLane(laneId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lane eliminado exitosamente", null));
    }
}
