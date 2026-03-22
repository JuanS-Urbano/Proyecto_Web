package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.MensajeRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.impl.MensajeServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MensajeServiceImplTest {

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private ProcesoRepository procesoRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HttpServletRequest httpServletRequest;


    @InjectMocks
    private MensajeServiceImpl mensajeService;

    private Empresa empresa;
    private Pool pool;
    private Proceso proceso;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        pool = new Pool();
        pool.setId(1L);
        pool.setEmpresa(empresa);

        proceso = new Proceso();
        proceso.setId(10L);
        proceso.setPool(pool);

        usuario = new Usuario();
        usuario.setId(99L);
        usuario.setEmail("user@empresa.com");
        usuario.setEmpresa(empresa);

        Mockito.lenient().when(httpServletRequest.getHeader("X-User-Email")).thenReturn(usuario.getEmail());
        Mockito.lenient().when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    // =====================================================================================
    // throwMessage tests
    // =====================================================================================

    @Test
    void throwMessage_exitoso_sinPayload() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        Mensaje guardado = new Mensaje();
        guardado.setId(1L);
        guardado.setNombre("Orden Aprobada");
        guardado.setTipo(TipoMensaje.THROW);
        guardado.setEstado(EstadoMensaje.PENDIENTE);
        guardado.setProceso(proceso);
        when(mensajeRepository.save(any())).thenReturn(guardado);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Orden Aprobada");
        dto.setProcesoId(proceso.getId());

        MensajeDTO result = mensajeService.throwMessage(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Orden Aprobada");
        assertThat(result.getTipo()).isEqualTo(TipoMensaje.THROW);
        assertThat(result.getEstado()).isEqualTo(EstadoMensaje.PENDIENTE);
        assertThat(result.getProcesoId()).isEqualTo(proceso.getId());
    }

    @Test
    void throwMessage_conPayloadJSON_valido() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        String jsonPayload = "{\"orderId\": 123, \"total\": 500.00}";

        Mensaje guardado = new Mensaje();
        guardado.setId(2L);
        guardado.setNombre("Pago Procesado");
        guardado.setPayloadJson(jsonPayload);
        guardado.setTipo(TipoMensaje.THROW);
        guardado.setEstado(EstadoMensaje.PENDIENTE);
        guardado.setProceso(proceso);
        when(mensajeRepository.save(any())).thenReturn(guardado);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Pago Procesado");
        dto.setPayloadJson(jsonPayload);
        dto.setProcesoId(proceso.getId());

        MensajeDTO result = mensajeService.throwMessage(dto);

        assertThat(result.getPayloadJson()).isEqualTo(jsonPayload);
    }

    @Test
    void throwMessage_conPayloadJSON_invalido_falla() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Mensaje Malo");
        dto.setPayloadJson("esto no es json");
        dto.setProcesoId(proceso.getId());

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JSON válido");
    }

    @Test
    void throwMessage_procesoNoExiste_falla() {
        when(procesoRepository.findById(999L)).thenReturn(Optional.empty());

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Mensaje");
        dto.setProcesoId(999L);

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proceso no encontrado");
    }

    @Test
    void throwMessage_sinHeaderUsuario_falla() {
        when(httpServletRequest.getHeader("X-User-Email")).thenReturn(null);
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Mensaje");
        dto.setProcesoId(proceso.getId());

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void throwMessage_nombreVacio_falla() {
        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("");
        dto.setProcesoId(proceso.getId());

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del mensaje");
    }

    @Test
    void throwMessage_conActividadOrigen_exitoso() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        Actividad actividad = new Actividad();
        actividad.setId(5L);
        actividad.setProceso(proceso);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        Mensaje guardado = new Mensaje();
        guardado.setId(3L);
        guardado.setNombre("Notificación");
        guardado.setTipo(TipoMensaje.THROW);
        guardado.setEstado(EstadoMensaje.PENDIENTE);
        guardado.setProceso(proceso);
        guardado.setActividadOrigen(actividad);
        when(mensajeRepository.save(any())).thenReturn(guardado);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Notificación");
        dto.setProcesoId(proceso.getId());
        dto.setActividadOrigenId(5L);

        MensajeDTO result = mensajeService.throwMessage(dto);

        assertThat(result.getActividadOrigenId()).isEqualTo(5L);
    }

    @Test
    void throwMessage_conActividadDeOtroProceso_falla() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        Proceso otroProceso = new Proceso();
        otroProceso.setId(999L);

        Actividad actividad = new Actividad();
        actividad.setId(5L);
        actividad.setProceso(otroProceso);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Notificación");
        dto.setProcesoId(proceso.getId());
        dto.setActividadOrigenId(5L);

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece al proceso");
    }

    // =====================================================================================
    // listar y obtener tests
    // =====================================================================================

    @Test
    void listarMensajesPorProceso_exitoso() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        Mensaje m1 = new Mensaje();
        m1.setId(1L);
        m1.setNombre("Msg1");
        m1.setTipo(TipoMensaje.THROW);
        m1.setEstado(EstadoMensaje.PENDIENTE);
        m1.setProceso(proceso);

        Mensaje m2 = new Mensaje();
        m2.setId(2L);
        m2.setNombre("Msg2");
        m2.setTipo(TipoMensaje.THROW);
        m2.setEstado(EstadoMensaje.ENTREGADO);
        m2.setProceso(proceso);

        when(mensajeRepository.findByProcesoId(proceso.getId())).thenReturn(List.of(m1, m2));

        List<MensajeDTO> result = mensajeService.listarMensajesPorProceso(proceso.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombre()).isEqualTo("Msg1");
        assertThat(result.get(1).getNombre()).isEqualTo("Msg2");
    }

    @Test
    void obtenerMensajePorId_exitoso() {
        Mensaje mensaje = new Mensaje();
        mensaje.setId(1L);
        mensaje.setNombre("Test Msg");
        mensaje.setTipo(TipoMensaje.THROW);
        mensaje.setEstado(EstadoMensaje.PENDIENTE);
        mensaje.setProceso(proceso);
        when(mensajeRepository.findById(1L)).thenReturn(Optional.of(mensaje));

        MensajeDTO result = mensajeService.obtenerMensajePorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Test Msg");
    }

    @Test
    void obtenerMensajePorId_noExiste_falla() {
        when(mensajeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mensajeService.obtenerMensajePorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mensaje no encontrado");
    }
}
