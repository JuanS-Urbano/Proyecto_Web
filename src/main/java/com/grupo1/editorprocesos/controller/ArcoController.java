package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.ArcoDTO;
import com.grupo1.editorprocesos.service.ArcoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/arcos")
@RequiredArgsConstructor
public class ArcoController {

    private final ArcoService arcoService;

    /**
     * HU-11: Crear un nuevo arco dentro de un proceso.
     * Body requerido: origenId, destinoId, procesoId.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ArcoDTO>> crearArco(@RequestBody ArcoDTO arcoDTO) {
        ArcoDTO creado = arcoService.crearArco(arcoDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Arco creado exitosamente", creado));
    }

    /**
     * HU-12: Editar un arco existente.
     * Solo se actualizan los campos enviados (parcial update).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArcoDTO>> editarArco(
            @PathVariable Long id,
            @RequestBody ArcoDTO arcoDTO) {
        ArcoDTO actualizado = arcoService.editarArco(id, arcoDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Arco actualizado exitosamente", actualizado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArcoDTO>> obtenerArco(@PathVariable Long id) {
        ArcoDTO arco = arcoService.obtenerArcoPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Arco obtenido exitosamente", arco));
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<ApiResponse<List<ArcoDTO>>> listarArcosPorProceso(@PathVariable Long procesoId) {
        List<ArcoDTO> arcos = arcoService.listarArcosPorProceso(procesoId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Arcos obtenidos exitosamente", arcos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarArco(@PathVariable Long id) {
        arcoService.eliminarArco(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Arco eliminado exitosamente", null));
    }
}