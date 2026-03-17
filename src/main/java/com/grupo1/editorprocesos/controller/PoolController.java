package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.PoolDTO;
import com.grupo1.editorprocesos.service.PoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pools")
@RequiredArgsConstructor
public class PoolController {

    private final PoolService poolService;

    @PostMapping
    public ResponseEntity<ApiResponse<PoolDTO>> crearPool(@RequestBody PoolDTO poolDTO) {
        PoolDTO creado = poolService.crearPool(poolDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Pool creado exitosamente", creado));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<ApiResponse<List<PoolDTO>>> listarPoolsPorEmpresa(@PathVariable Long empresaId) {
        List<PoolDTO> pools = poolService.listarPoolsPorEmpresa(empresaId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pools de la empresa obtenidos", pools));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PoolDTO>> editarPool(@PathVariable Long id, @RequestBody PoolDTO poolDTO) {
        PoolDTO actualizado = poolService.editarPool(id, poolDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pool actualizado exitosamente", actualizado));
    }
}
