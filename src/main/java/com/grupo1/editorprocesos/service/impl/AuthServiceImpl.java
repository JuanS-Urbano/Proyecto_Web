package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.AuthRequestDTO;
import com.grupo1.editorprocesos.dto.AuthResponseDTO;
import com.grupo1.editorprocesos.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken("mock-jwt-token-123");
        response.setEmail(request.getEmail());
        response.setUsuarioId(1L);
        return response;
    }
}
