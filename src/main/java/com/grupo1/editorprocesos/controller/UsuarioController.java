package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.UsuarioCreateDTO;
import com.grupo1.editorprocesos.dto.UsuarioDTO;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> crearUsuario(@RequestBody UsuarioCreateDTO usuarioCreateDTO) {
        UsuarioDTO creado = usuarioService.crearUsuario(usuarioCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Usuario creado exitosamente", creado));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> listarUsuariosPorEmpresa(@PathVariable Long empresaId) {
        List<UsuarioDTO> usuarios = usuarioService.listarUsuariosPorEmpresa(empresaId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Usuarios obtenidos exitosamente", usuarios));
    }

    @PutMapping("/{id}/rol")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> cambiarRol(
            @PathVariable Long id,
            @RequestParam RolSistema nuevoRol) {
        UsuarioDTO actualizado = usuarioService.cambiarRol(id, nuevoRol);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rol de usuario actualizado exitosamente", actualizado));
    }
}
