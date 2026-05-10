package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.config.JwtService;
import com.grupo1.editorprocesos.dto.AuthRequestDTO;
import com.grupo1.editorprocesos.dto.AuthResponseDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.repository.PoolRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PoolRepository poolRepository;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthRequestDTO request) {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró usuario con el email: " + request.getEmail()));

        // 2. Validar que el usuario esté activo
        if (Boolean.FALSE.equals(usuario.getIsActivo())) {
            throw new UnauthorizedException("La cuenta del usuario está desactivada");
        }

        // 3. Validar contraseña con BCrypt
        String rawPassword = request.getPassword() != null ? request.getPassword().trim() : "";
        if (!passwordEncoder.matches(rawPassword, usuario.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        // 4. Construir respuesta
        AuthResponseDTO response = new AuthResponseDTO();
        Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
        response.setToken(jwtService.generateToken(usuario.getEmail(), usuario.getRolSistema().name(), empresaId));
        response.setUsuario(new com.grupo1.editorprocesos.dto.ReferenciaDTO(usuario.getId(), usuario.getEmail()));
        response.setEmail(usuario.getEmail());
        response.setRolSistema(usuario.getRolSistema().name());
        if (usuario.getEmpresa() != null) {
            response.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(
                    usuario.getEmpresa().getId(), usuario.getEmpresa().getNombre()));
            // Incluir poolId del primer pool de la empresa para que el frontend lo use
            poolRepository.findByEmpresaId(usuario.getEmpresa().getId())
                    .stream().findFirst()
                    .ifPresent(pool -> response.setPoolId(pool.getId()));
        }

        return response;
    }
}
