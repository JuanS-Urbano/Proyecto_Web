package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.UsuarioCreateDTO;
import com.grupo1.editorprocesos.dto.UsuarioDTO;
import com.grupo1.editorprocesos.exception.DuplicateResourceException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioDTO crearUsuario(UsuarioCreateDTO usuarioCreate) {
        // 1. Validar que no exista otro usuario con el mismo email
        if (usuarioRepository.findByEmail(usuarioCreate.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el email: "
                    + usuarioCreate.getEmail());
        }

        // 2. Validar que la empresa (tenant) proporcionada exista
        Empresa empresa = empresaRepository.findById(usuarioCreate.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: "
                        + usuarioCreate.getEmpresaId()));

        // 3. Mapear DTO de creación a la entidad
        Usuario usuario = modelMapper.map(usuarioCreate, Usuario.class);

        // 4. Hashear la contraseña con BCrypt
        usuario.setPassword(passwordEncoder.encode(usuarioCreate.getPassword()));

        // 5. Asignar valores adicionales: empresa, rol base y activo
        usuario.setEmpresa(empresa);
        usuario.setRolSistema(RolSistema.LECTOR);
        usuario.setIsActivo(true);

        // 6. Persistir el usuario
        Usuario guardado = usuarioRepository.save(usuario);

        // 7. Devolver DTO de salida
        UsuarioDTO salida = modelMapper.map(guardado, UsuarioDTO.class);
        salida.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(empresa.getId(), empresa.getNombre()));
        return salida;
    }

    @Override
    @Transactional
    public String crearAdminInicial(Empresa empresa, String emailContacto) {
        if (usuarioRepository.findByEmail(emailContacto).isPresent()) {
            throw new DuplicateResourceException(
                    "El correo de contacto ya está registrado como usuario: " + emailContacto
                    + ". Use un correo distinto para la empresa.");
        }

        String generatedPassword = java.util.UUID.randomUUID().toString();

        Usuario admin = new Usuario();
        admin.setEmail(emailContacto);
        admin.setPassword(passwordEncoder.encode(generatedPassword));
        admin.setEmpresa(empresa);
        admin.setRolSistema(RolSistema.ADMIN_EMPRESA);
        admin.setIsActivo(true);

        usuarioRepository.save(admin);
        return generatedPassword;
    }
    
    @Override
    @Transactional(readOnly = true)
    public java.util.List<UsuarioDTO> listarUsuariosPorEmpresa(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new ResourceNotFoundException("Empresa no encontrada con id: " + empresaId);
        }
        return usuarioRepository.findByEmpresaId(empresaId).stream()
                .map(usuario -> {
                    UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
                    dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(usuario.getEmpresa().getId(), usuario.getEmpresa().getNombre()));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public UsuarioDTO cambiarRol(Long usuarioId, RolSistema nuevoRol) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
        
        usuario.setRolSistema(nuevoRol);
        Usuario guardado = usuarioRepository.save(usuario);
        
        UsuarioDTO salida = modelMapper.map(guardado, UsuarioDTO.class);
        salida.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(guardado.getEmpresa().getId(), guardado.getEmpresa().getNombre()));
        return salida;
    }
}
