package com.grupo1.editorprocesos.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grupo1.editorprocesos.dto.ArcoDTO;
import com.grupo1.editorprocesos.exception.DuplicateResourceException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import com.grupo1.editorprocesos.model.entity.bpmn.Gateway;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.ArcoRepository;
import com.grupo1.editorprocesos.repository.GatewayRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.service.impl.ArcoServiceImpl;


@ExtendWith(MockitoExtension.class)
class ArcoServiceImplTest {

    @Mock
    private ArcoRepository arcoRepository;

    @Mock
    private com.grupo1.editorprocesos.service.PermisosPoolService permisosPoolService;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private com.grupo1.editorprocesos.service.UsuarioActualService usuarioActualService;

    @Mock
    private HistorialCambiosRepository historialCambiosRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private GatewayRepository gatewayRepository;



    @InjectMocks
    private ArcoServiceImpl arcoService;

    private Empresa empresa;
    private Usuario usuario;
    private Pool pool;
    private Proceso proceso;
    private Arco arco;
    private Actividad actividad1;
    private Actividad actividad2;
    private Gateway gateway;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("test@empresa.com");
        usuario.setEmpresa(empresa);

        pool = new Pool();
        pool.setId(2L);
        pool.setEmpresa(empresa);

        proceso = new Proceso();
        proceso.setId(5L);
        proceso.setPool(pool);

        arco = new Arco();
        arco.setId(20L);
        arco.setOrigenId("101");
        arco.setDestinoId("102");
        arco.setProceso(proceso);

        actividad1 = new Actividad();
        actividad1.setId(101L);
        actividad1.setProceso(proceso);

        actividad2 = new Actividad();
        actividad2.setId(102L);
        actividad2.setProceso(proceso);

        gateway = new Gateway();
        gateway.setId(104L);
        gateway.setProceso(proceso);

        org.mockito.Mockito.lenient().when(usuarioActualService.obtenerUsuarioActual()).thenReturn(usuario);
    }

    @Test
    void crearArco_exitoso() {
        ArcoDTO dto = new ArcoDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));
        dto.setOrigenId("101");
        dto.setDestinoId("102");

        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);
        when(actividadRepository.findById(101L)).thenReturn(Optional.of(actividad1));
        when(actividadRepository.findById(102L)).thenReturn(Optional.of(actividad2));
        when(arcoRepository.findByOrigenIdAndDestinoIdAndProcesoId("101", "102", 5L)).thenReturn(Optional.empty());
        when(arcoRepository.save(any(Arco.class))).thenReturn(arco);

        ArcoDTO result = arcoService.crearArco(dto);

        assertThat(result.getId()).isEqualTo(20L);
        verify(historialCambiosRepository).save(any(HistorialCambios.class));
    }

    @Test
    void crearArco_mismaReferencia() {
        ArcoDTO dto = new ArcoDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));
        dto.setOrigenId("101");
        dto.setDestinoId("101");

        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);
        when(actividadRepository.findById(101L)).thenReturn(Optional.of(actividad1));

        assertThatThrownBy(() -> arcoService.crearArco(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mismo elemento");
    }

    @Test
    void crearArco_elementoNoEncontrado() {
        ArcoDTO dto = new ArcoDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));
        dto.setOrigenId("103");
        dto.setDestinoId("102");

        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);
        when(actividadRepository.findById(103L)).thenReturn(Optional.empty());
        when(gatewayRepository.findById(103L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> arcoService.crearArco(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void crearArco_duplicado() {
        ArcoDTO dto = new ArcoDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));
        dto.setOrigenId("101");
        dto.setDestinoId("102");

        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);
        when(actividadRepository.findById(101L)).thenReturn(Optional.of(actividad1));
        when(actividadRepository.findById(102L)).thenReturn(Optional.of(actividad2));
        when(arcoRepository.findByOrigenIdAndDestinoIdAndProcesoId("101", "102", 5L)).thenReturn(Optional.of(arco));

        assertThatThrownBy(() -> arcoService.crearArco(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe un arco");
    }

    @Test
    void editarArco_exitoso() {
        ArcoDTO dto = new ArcoDTO();
        dto.setOrigenId("104");

        when(arcoRepository.findById(20L)).thenReturn(Optional.of(arco));
        when(actividadRepository.findById(104L)).thenReturn(Optional.empty());
        when(gatewayRepository.findById(104L)).thenReturn(Optional.of(gateway));
        when(arcoRepository.findByOrigenIdAndDestinoIdAndProcesoId("104", "102", 5L)).thenReturn(Optional.empty());
        when(arcoRepository.save(arco)).thenReturn(arco);

        ArcoDTO result = arcoService.editarArco(20L, dto);

        assertThat(result.getOrigenId()).isEqualTo("104");
        verify(historialCambiosRepository).save(any(HistorialCambios.class));
    }

    @Test
    void eliminarArco_exitoso() {
        when(arcoRepository.findById(20L)).thenReturn(Optional.of(arco));

        arcoService.eliminarArco(20L);

        verify(arcoRepository).delete(arco);
        verify(historialCambiosRepository).save(any(HistorialCambios.class));
    }

    @Test
    void obtenerArcoPorId_exitoso() {
        when(arcoRepository.findById(20L)).thenReturn(Optional.of(arco));
        ArcoDTO result = arcoService.obtenerArcoPorId(20L);
        assertThat(result.getId()).isEqualTo(20L);
    }

    @Test
    void obtenerArcoPorId_noEncontrado() {
        when(arcoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> arcoService.obtenerArcoPorId(99L))
                .isInstanceOf(com.grupo1.editorprocesos.exception.ResourceNotFoundException.class);
    }

    @Test
    void editarArco_noEncontrado() {
        ArcoDTO dto = new ArcoDTO();
        when(arcoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> arcoService.editarArco(99L, dto))
                .isInstanceOf(com.grupo1.editorprocesos.exception.ResourceNotFoundException.class);
    }

    @Test
    void eliminarArco_noEncontrado() {
        when(arcoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> arcoService.eliminarArco(99L))
                .isInstanceOf(com.grupo1.editorprocesos.exception.ResourceNotFoundException.class);
    }
}
