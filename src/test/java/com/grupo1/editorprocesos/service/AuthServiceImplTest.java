package com.grupo1.editorprocesos.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.grupo1.editorprocesos.dto.AuthRequestDTO;
import com.grupo1.editorprocesos.dto.AuthResponseDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuario;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("test@empresa.com");
        usuario.setPassword("hashedPassword");
        usuario.setIsActivo(true);
        usuario.setRolSistema(RolSistema.ADMIN_EMPRESA);
        usuario.setEmpresa(empresa);
    }

    @Test
    void login_exitoso() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("test@empresa.com");
        request.setPassword("plainPassword");

        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);

        AuthResponseDTO response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsuario().getId()).isEqualTo(10L);
        assertThat(response.getEmail()).isEqualTo("test@empresa.com");
        assertThat(response.getRolSistema()).isEqualTo("ADMIN_EMPRESA");
        assertThat(response.getEmpresa().getId()).isEqualTo(1L);
    }

    @Test
    void login_usuarioNoEncontrado() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("noexiste@empresa.com");
        request.setPassword("password");

        when(usuarioRepository.findByEmail("noexiste@empresa.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("noexiste@empresa.com");
    }

    @Test
    void login_usuarioInactivo() {
        usuario.setIsActivo(false);

        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("test@empresa.com");
        request.setPassword("password");

        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("desactivada");
    }

    @Test
    void login_passwordIncorrecto() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("test@empresa.com");
        request.setPassword("wrongPassword");

        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Credenciales");
    }

    @Test
    void login_sinEmpresa_noSetEmpresaId() {
        usuario.setEmpresa(null);

        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("test@empresa.com");
        request.setPassword("plainPassword");

        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);

        AuthResponseDTO response = authService.login(request);

        assertThat(response.getEmpresa()).isNull();
    }

    @Test
    void login_retornaTokenConIdDeUsuario() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("test@empresa.com");
        request.setPassword("plainPassword");

        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);

        AuthResponseDTO response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("session-10");
    }

    @Test
    void login_conRolLector_retornaRolCorrecto() {
        usuario.setRolSistema(RolSistema.LECTOR);

        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("test@empresa.com");
        request.setPassword("plainPassword");

        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);

        AuthResponseDTO response = authService.login(request);

        assertThat(response.getRolSistema()).isEqualTo("LECTOR");
    }
}

