package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.PoolDTO;
import com.grupo1.editorprocesos.service.PoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
