package com.grupo1.editorprocesos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.service.impl.PermisosPoolServiceImpl;

class PermisosPoolServiceImplTest {

    private PermisosPoolServiceImpl permisosPoolService;

    @BeforeEach
    void setUp() {
        permisosPoolService = new PermisosPoolServiceImpl();
    }

    // ===== tienePermisoEscritura =====

    @Test
    void tienePermisoEscritura_adminPlataforma_retornaTrue() {
        Usuario usuario = crearUsuarioConRol(RolSistema.ADMIN_PLATAFORMA);
        assertThat(permisosPoolService.tienePermisoEscritura(usuario)).isTrue();
    }

    @Test
    void tienePermisoEscritura_adminEmpresa_retornaTrue() {
        Usuario usuario = crearUsuarioConRol(RolSistema.ADMIN_EMPRESA);
        assertThat(permisosPoolService.tienePermisoEscritura(usuario)).isTrue();
    }

    @Test
    void tienePermisoEscritura_editor_retornaTrue() {
        Usuario usuario = crearUsuarioConRol(RolSistema.EDITOR);
        assertThat(permisosPoolService.tienePermisoEscritura(usuario)).isTrue();
    }

    @Test
    void tienePermisoEscritura_lector_retornaFalse() {
        Usuario usuario = crearUsuarioConRol(RolSistema.LECTOR);
        assertThat(permisosPoolService.tienePermisoEscritura(usuario)).isFalse();
    }

    @Test
    void tienePermisoEscritura_usuarioNulo_retornaFalse() {
        assertThat(permisosPoolService.tienePermisoEscritura(null)).isFalse();
    }

    @Test
    void tienePermisoEscritura_rolNulo_retornaFalse() {
        Usuario usuario = new Usuario();
        usuario.setRolSistema(null);
        assertThat(permisosPoolService.tienePermisoEscritura(usuario)).isFalse();
    }

    // ===== validarPermisoEscritura =====

    @Test
    void validarPermisoEscritura_adminPlataforma_noLanzaExcepcion() {
        Usuario usuario = crearUsuarioConRol(RolSistema.ADMIN_PLATAFORMA);
        permisosPoolService.validarPermisoEscritura(usuario); // No debe lanzar excepción
    }

    @Test
    void validarPermisoEscritura_editor_noLanzaExcepcion() {
        Usuario usuario = crearUsuarioConRol(RolSistema.EDITOR);
        permisosPoolService.validarPermisoEscritura(usuario); // No debe lanzar excepción
    }

    @Test
    void validarPermisoEscritura_lector_lanzaUnauthorizedException() {
        Usuario usuario = crearUsuarioConRol(RolSistema.LECTOR);
        assertThatThrownBy(() -> permisosPoolService.validarPermisoEscritura(usuario))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("no tiene permisos de escritura");
    }

    // ===== validarPermisoLectura =====

    @Test
    void validarPermisoLectura_todosLosRoles_noLanzaExcepcion() {
        for (RolSistema rol : RolSistema.values()) {
            Usuario usuario = crearUsuarioConRol(rol);
            permisosPoolService.validarPermisoLectura(usuario); // Ninguno debe lanzar excepción
        }
    }

    @Test
    void validarPermisoLectura_usuarioNulo_lanzaUnauthorizedException() {
        assertThatThrownBy(() -> permisosPoolService.validarPermisoLectura(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Usuario sin rol asignado");
    }

    @Test
    void validarPermisoLectura_rolNulo_lanzaUnauthorizedException() {
        Usuario usuario = new Usuario();
        usuario.setRolSistema(null);
        assertThatThrownBy(() -> permisosPoolService.validarPermisoLectura(usuario))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Usuario sin rol asignado");
    }

    // ===== Helper =====

    private Usuario crearUsuarioConRol(RolSistema rol) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");
        usuario.setRolSistema(rol);
        return usuario;
    }
}
