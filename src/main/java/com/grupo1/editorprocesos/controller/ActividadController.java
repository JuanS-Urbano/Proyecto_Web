package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ActividadDTO;
import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.service.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService actividadService;

    /**
     * HU-08: Crear una nueva actividad dentro de un proceso.
     * Body requerido: nombre, tipoActividad, procesoId.
     * Opcionales: posicionX, posicionY, laneId.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ActividadDTO>> crearActividad(@RequestBody ActividadDTO actividadDTO) {
        ActividadDTO creada = actividadService.crearActividad(actividadDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Actividad creada exitosamente", creada));
    }

    /**
     * HU-09: Editar una actividad existente.
     * Solo se actualizan los campos enviados (parcial update).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadDTO>> editarActividad(
            @PathVariable Long id,
            @RequestBody ActividadDTO actividadDTO) {
        ActividadDTO actualizada = actividadService.editarActividad(id, actividadDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Actividad actualizada exitosamente", actualizada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadDTO>> obtenerActividad(@PathVariable Long id) {
        ActividadDTO actividad = actividadService.obtenerActividadPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Actividad obtenida exitosamente", actividad));
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<ApiResponse<List<ActividadDTO>>> listarActividadesPorProceso(@PathVariable Long procesoId) {
        List<ActividadDTO> actividades = actividadService.listarActividadesPorProceso(procesoId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Actividades del proceso obtenidas exitosamente", actividades));
    }
}
