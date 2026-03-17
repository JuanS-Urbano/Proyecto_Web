package com.grupo1.editorprocesos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
            @RequestBody LaneDTO laneDTO) {
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
}
