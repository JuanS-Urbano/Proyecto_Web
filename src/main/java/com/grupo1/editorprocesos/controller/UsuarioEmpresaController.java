package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.UsuarioEmpresaDTO;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.service.UsuarioEmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/empresa/usuarios")
public class UsuarioEmpresaController {

    private final UsuarioEmpresaService usuarioEmpresaService;

    public UsuarioEmpresaController(UsuarioEmpresaService usuarioEmpresaService) {
        this.usuarioEmpresaService = usuarioEmpresaService;
    }

    @GetMapping("/{empresaId}")
    public String listarUsuarios(@PathVariable Long empresaId, Model model) {
        model.addAttribute("usuarios", usuarioEmpresaService.listarUsuariosPorEmpresa(empresaId));
        model.addAttribute("empresa", usuarioEmpresaService.obtenerEmpresaPorId(empresaId));
        model.addAttribute("empresaId", empresaId);
        return "usuariosEmpresa/lista";
    }

    @GetMapping("/nuevo/{empresaId}")
    public String mostrarFormulario(@PathVariable Long empresaId, Model model) {
        UsuarioEmpresaDTO dto = new UsuarioEmpresaDTO();
        dto.setEmpresaId(empresaId);

        model.addAttribute("usuarioDTO", dto);
        model.addAttribute("roles", RolSistema.values());
        model.addAttribute("empresaId", empresaId);
        return "usuariosEmpresa/formulario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuarioDTO") UsuarioEmpresaDTO usuarioDTO) {
        usuarioEmpresaService.guardarUsuario(usuarioDTO);
        return "redirect:/empresa/usuarios/" + usuarioDTO.getEmpresaId();
    }
}