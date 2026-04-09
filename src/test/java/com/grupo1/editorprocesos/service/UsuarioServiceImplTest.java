package com.grupo1.editorprocesos.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.grupo1.editorprocesos.dto.UsuarioCreateDTO;
import com.grupo1.editorprocesos.dto.UsuarioDTO;
import com.grupo1.editorprocesos.exception.DuplicateResourceException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.impl.UsuarioServiceImpl;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
    }

    @Test
    void crearUsuario_exitoso() {
        UsuarioCreateDTO createDTO = new UsuarioCreateDTO();
        createDTO.setEmail("nuevo@empresa.com");
        createDTO.setPassword("password123");
        createDTO.setEmpresaId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(10L);

        UsuarioDTO dtoSalida = new UsuarioDTO();
        dtoSalida.setEmail("nuevo@empresa.com");

        when(usuarioRepository.findByEmail("nuevo@empresa.com")).thenReturn(Optional.empty());
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(modelMapper.map(createDTO, Usuario.class)).thenReturn(usuario);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(modelMapper.map(usuario, UsuarioDTO.class)).thenReturn(dtoSalida);

        UsuarioDTO result = usuarioService.crearUsuario(createDTO);

        assertThat(result.getEmail()).isEqualTo("nuevo@empresa.com");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void crearUsuario_emailDuplicado() {
        UsuarioCreateDTO createDTO = new UsuarioCreateDTO();
        createDTO.setEmail("existe@empresa.com");

        when(usuarioRepository.findByEmail("existe@empresa.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> usuarioService.crearUsuario(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("existe@empresa.com");
    }

    @Test
    void crearUsuario_empresaNoEncontrada() {
        UsuarioCreateDTO createDTO = new UsuarioCreateDTO();
        createDTO.setEmail("nuevo@empresa.com");
        createDTO.setEmpresaId(99L);

        when(usuarioRepository.findByEmail("nuevo@empresa.com")).thenReturn(Optional.empty());
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.crearUsuario(createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crearAdminInicial_exitoso() {
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashedRandom");

        usuarioService.crearAdminInicial(empresa, "admin@empresa.com");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario admin = captor.getValue();
        assertThat(admin.getEmail()).isEqualTo("admin@empresa.com");
        assertThat(admin.getRolSistema()).isEqualTo(RolSistema.ADMIN_EMPRESA);
        assertThat(admin.getIsActivo()).isTrue();
        assertThat(admin.getEmpresa()).isEqualTo(empresa);
    }

    @Test
    void crearUsuario_asignaRolLector_yActivo() {
        UsuarioCreateDTO createDTO = new UsuarioCreateDTO();
        createDTO.setEmail("lector@empresa.com");
        createDTO.setPassword("pass123");
        createDTO.setEmpresaId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(11L);

        UsuarioDTO dtoSalida = new UsuarioDTO();
        dtoSalida.setEmail("lector@empresa.com");

        when(usuarioRepository.findByEmail("lector@empresa.com")).thenReturn(Optional.empty());
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(modelMapper.map(createDTO, Usuario.class)).thenReturn(usuario);
        when(passwordEncoder.encode("pass123")).thenReturn("hashedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(modelMapper.map(usuario, UsuarioDTO.class)).thenReturn(dtoSalida);

        usuarioService.crearUsuario(createDTO);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario saved = captor.getValue();
        assertThat(saved.getRolSistema()).isEqualTo(RolSistema.LECTOR);
        assertThat(saved.getIsActivo()).isTrue();
        assertThat(saved.getPassword()).isEqualTo("hashedPass");
    }

    @Test
    void crearUsuario_estableceEmpresaCorrecta() {
        UsuarioCreateDTO createDTO = new UsuarioCreateDTO();
        createDTO.setEmail("otro@empresa.com");
        createDTO.setPassword("pass");
        createDTO.setEmpresaId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(12L);

        UsuarioDTO dtoSalida = new UsuarioDTO();

        when(usuarioRepository.findByEmail("otro@empresa.com")).thenReturn(Optional.empty());
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(modelMapper.map(createDTO, Usuario.class)).thenReturn(usuario);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(modelMapper.map(usuario, UsuarioDTO.class)).thenReturn(dtoSalida);

        UsuarioDTO result = usuarioService.crearUsuario(createDTO);

        assertThat(result.getEmpresa().getId()).isEqualTo(1L);
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getEmpresa()).isEqualTo(empresa);
    }
}

