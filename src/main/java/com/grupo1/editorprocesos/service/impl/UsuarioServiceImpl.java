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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UsuarioDTO crearUsuario(UsuarioCreateDTO usuarioCreate) {
        // 1. validar que no exista otro usuario con el mismo email
        if (usuarioRepository.findByEmail(usuarioCreate.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el email: "
                    + usuarioCreate.getEmail());
        }

        // 2. validar que la empresa (tenant) proporcionada exista
        Empresa empresa = empresaRepository.findById(usuarioCreate.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: "
                        + usuarioCreate.getEmpresaId()));

        // 3. mapear DTO de creación a la entidad
        Usuario usuario = modelMapper.map(usuarioCreate, Usuario.class);

        // 4. asignar valores adicionales: empresa, rol base y activo
        usuario.setEmpresa(empresa);
        usuario.setRolSistema(RolSistema.LECTOR); // rol base por defecto, ajustar según política
        usuario.setIsActivo(true);

        // 5. persistir el usuario
        Usuario guardado = usuarioRepository.save(usuario);

        // 6. devolver DTO de salida
        UsuarioDTO salida = modelMapper.map(guardado, UsuarioDTO.class);
        salida.setEmpresaId(empresa.getId());
        return salida;
    }

    @Override
    @Transactional
    public void crearAdminInicial(Empresa empresa, String emailContacto) {
        // En una implementación real, se generaría una contraseña aleatoria o temporal,
        // o se enviaría un correo de activación.

        Usuario admin = new Usuario();
        admin.setEmail(emailContacto);
        admin.setPassword("admin123"); // Todo: Encriptar luego
        admin.setEmpresa(empresa);
        admin.setRolSistema(RolSistema.ADMIN_EMPRESA);
        admin.setIsActivo(true);

        usuarioRepository.save(admin);
    }
}
