package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.UsuarioActualService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioActualServiceImpl implements UsuarioActualService {

    private final HttpServletRequest httpServletRequest;
    private final UsuarioRepository usuarioRepository;

    @Override
    public Usuario obtenerUsuarioActual() {
        String email = httpServletRequest.getHeader("X-User-Email");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("No se proporcionó el header X-User-Email para identificar al usuario");
        }
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        "Usuario no encontrado con el email: " + email));
    }
}
