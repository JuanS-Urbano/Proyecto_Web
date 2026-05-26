package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String SECRET = "G3d!P9rXqZ#2mK7vNs8eYwQ4tL6bJcAhU0iF5oRxCpWaDnMkTjE1yuBzlHsIVfOg";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L); // 1 hour
    }

    @Test
    void testGenerateAndExtractToken() {
        String email = "user@empresa.com";
        String rol = "ADMIN_EMPRESA";
        Long empresaId = 5L;

        String token = jwtService.generateToken(email, rol, empresaId);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
        assertThat(jwtService.extractRol(token)).isEqualTo(rol);
        assertThat(jwtService.extractEmpresaId(token)).isEqualTo(empresaId);
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.string";
        assertThat(jwtService.isTokenValid(invalidToken)).isFalse();
    }

    @Test
    void testTokenExpired() {
        // Create an expired token by setting expirationMs to a negative value
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String token = jwtService.generateToken("user@empresa.com", "LECTOR", 1L);
        
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}
