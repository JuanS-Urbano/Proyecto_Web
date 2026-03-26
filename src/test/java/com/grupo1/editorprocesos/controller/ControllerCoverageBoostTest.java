package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.*;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import com.grupo1.editorprocesos.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerCoverageBoostTest {

    @Mock
    private ActividadService actividadService;
    @Mock
    private ArcoService arcoService;
    @Mock
    private GatewayService gatewayService;
    @Mock
    private LaneService laneService;
    @Mock
    private ProcesoService procesoService;
    @Mock
    private PoolService poolService;
    @Mock
    private RolProcesoService rolProcesoService;
    @Mock
    private MensajeService mensajeService;
    @Mock
    private NotificacionService notificacionService;
    @Mock
    private AuthService authService;

    @InjectMocks
    private ActividadController actividadController;
    @InjectMocks
    private ArcoController arcoController;
    @InjectMocks
    private GatewayController gatewayController;
    @InjectMocks
    private LaneController laneController;
    @InjectMocks
    private ProcesoController procesoController;
    @InjectMocks
    private PoolController poolController;
    @InjectMocks
    private RolProcesoController rolProcesoController;
    @InjectMocks
    private MensajeController mensajeController;
    @InjectMocks
    private NotificacionController notificacionController;
    @InjectMocks
    private AuthController authController;

    @Test
    void actividad_controller_cubre_crud_y_confirmacion() {
        ActividadDTO dto = new ActividadDTO();
        when(actividadService.crearActividad(dto)).thenReturn(dto);
        when(actividadService.editarActividad(1L, dto)).thenReturn(dto);
        when(actividadService.obtenerActividadPorId(1L)).thenReturn(dto);
        when(actividadService.listarActividadesPorProceso(10L)).thenReturn(List.of(dto));

        ResponseEntity<ApiResponse<ActividadDTO>> crear = actividadController.crearActividad(dto);
        ResponseEntity<ApiResponse<ActividadDTO>> editar = actividadController.editarActividad(1L, dto);
        ResponseEntity<ApiResponse<ActividadDTO>> obtener = actividadController.obtenerActividad(1L);
        ResponseEntity<ApiResponse<List<ActividadDTO>>> listar = actividadController.listarActividadesPorProceso(10L);
        ResponseEntity<ApiResponse<Void>> eliminarSinConfirmar = actividadController.eliminarActividad(1L, false);
        ResponseEntity<ApiResponse<Void>> eliminarConfirmado = actividadController.eliminarActividad(1L, true);

        assertThat(crear.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(editar.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(obtener.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listar.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eliminarSinConfirmar.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(eliminarConfirmado.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(actividadService).eliminarActividad(1L);
    }

    @Test
    void arco_controller_cubre_crud() {
        ArcoDTO dto = new ArcoDTO();
        when(arcoService.crearArco(dto)).thenReturn(dto);
        when(arcoService.editarArco(1L, dto)).thenReturn(dto);
        when(arcoService.obtenerArcoPorId(1L)).thenReturn(dto);
        when(arcoService.listarArcosPorProceso(10L)).thenReturn(List.of(dto));

        assertThat(arcoController.crearArco(dto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(arcoController.editarArco(1L, dto).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(arcoController.obtenerArco(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(arcoController.listarArcosPorProceso(10L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(arcoController.eliminarArco(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(arcoService).eliminarArco(1L);
    }

    @Test
    void gateway_controller_cubre_crud_y_confirmacion() {
        GatewayDTO dto = new GatewayDTO();
        when(gatewayService.crearGateway(dto)).thenReturn(dto);
        when(gatewayService.editarGateway(1L, dto)).thenReturn(dto);
        when(gatewayService.obtenerGatewayPorId(1L)).thenReturn(dto);
        when(gatewayService.listarGatewaysPorProceso(10L)).thenReturn(List.of(dto));

        assertThat(gatewayController.crearGateway(dto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(gatewayController.editarGateway(1L, dto).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(gatewayController.obtenerGateway(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(gatewayController.listarGatewaysPorProceso(10L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(gatewayController.eliminarGateway(1L, false).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(gatewayController.eliminarGateway(1L, true).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(gatewayService).eliminarGateway(1L);
    }

    @Test
    void lane_controller_cubre_crud_y_confirmacion() {
        LaneDTO dto = new LaneDTO();
        when(laneService.crearLane(10L, dto)).thenReturn(dto);
        when(laneService.obtenerLanePorId(1L)).thenReturn(dto);
        when(laneService.editarLane(1L, dto)).thenReturn(dto);
        when(laneService.listarLanesPorProceso(10L)).thenReturn(List.of(dto));

        assertThat(laneController.crearLane(10L, dto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(laneController.obtenerLane(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(laneController.editarLane(1L, dto).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(laneController.listarLanesPorProceso(10L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(laneController.eliminarLane(1L, false).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(laneController.eliminarLane(1L, true).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(laneService).eliminarLane(1L);
    }

    @Test
    void proceso_controller_cubre_endpoints() {
        ProcesoDTO proceso = new ProcesoDTO();
        HistorialCambiosDTO historial = new HistorialCambiosDTO();

        when(procesoService.crearProceso(proceso)).thenReturn(proceso);
        when(procesoService.obtenerProcesoById(1L)).thenReturn(proceso);
        when(procesoService.listarProcesosPorEmpresa(1L)).thenReturn(List.of(proceso));
        when(procesoService.listarProcesosPorPool(2L)).thenReturn(List.of(proceso));
        when(procesoService.listarProcesosPorPoolYEstado(2L, EstadoProceso.BORRADOR)).thenReturn(List.of(proceso));
        when(procesoService.listarProcesosPorPoolYCategoria(2L, "cat")).thenReturn(List.of(proceso));
        when(procesoService.listarProcesosPorPoolConFiltros(2L, EstadoProceso.BORRADOR, "cat")).thenReturn(List.of(proceso));
        when(procesoService.buscarProcesosPorNombre(2L, "nombre")).thenReturn(List.of(proceso));
        when(procesoService.editarProceso(1L, proceso)).thenReturn(proceso);
        when(procesoService.obtenerHistorialProceso(1L)).thenReturn(List.of(historial));

        assertThat(procesoController.crearProceso(proceso).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(procesoController.obtenerProceso(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.listarProcesosPorEmpresa(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.listarProcesosPorPool(2L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.listarProcesosPorPoolYEstado(2L, EstadoProceso.BORRADOR).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.listarProcesosPorPoolYCategoria(2L, "cat").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.listarProcesosPorPoolConFiltros(2L, EstadoProceso.BORRADOR, "cat").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.buscarProcesosPorNombre(2L, "nombre").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.editarProceso(1L, proceso).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.eliminarProceso(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(procesoController.obtenerHistorialProceso(1L).getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(procesoService).eliminarProceso(1L);
    }

    @Test
    void pool_controller_cubre_endpoints() {
        PoolDTO pool = new PoolDTO();
        when(poolService.crearPool(pool)).thenReturn(pool);
        when(poolService.listarPoolsPorEmpresa(1L)).thenReturn(List.of(pool));
        when(poolService.editarPool(1L, pool)).thenReturn(pool);

        assertThat(poolController.crearPool(pool).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(poolController.listarPoolsPorEmpresa(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(poolController.editarPool(1L, pool).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(poolController.eliminarPool(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(poolService).eliminarPool(1L);
    }

    @Test
    void rol_proceso_controller_cubre_endpoints() {
        RolProcesoDTO rol = new RolProcesoDTO();
        when(rolProcesoService.crearRol(rol)).thenReturn(rol);
        when(rolProcesoService.editarRol(1L, rol)).thenReturn(rol);
        when(rolProcesoService.listarPorEmpresa(1L)).thenReturn(List.of(rol));
        when(rolProcesoService.obtenerPorId(1L)).thenReturn(rol);

        assertThat(rolProcesoController.crearRol(rol).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(rolProcesoController.editarRol(1L, rol).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rolProcesoController.eliminarRol(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rolProcesoController.listarPorEmpresa(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rolProcesoController.obtenerPorId(1L).getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(rolProcesoService).eliminarRol(1L);
    }

    @Test
    void mensaje_controller_cubre_endpoints() {
        MensajeDTO mensaje = new MensajeDTO();
        CorrelacionResultDTO correlacion = new CorrelacionResultDTO();
        correlacion.setMensaje("ok");

        when(mensajeService.throwMessage(mensaje)).thenReturn(mensaje);
        when(mensajeService.catchMessage(mensaje)).thenReturn(mensaje);
        when(mensajeService.listarMensajesPorProceso(1L)).thenReturn(List.of(mensaje));
        when(mensajeService.obtenerMensajePorId(1L)).thenReturn(mensaje);
        when(mensajeService.correlateMessages("key-1")).thenReturn(correlacion);
        when(mensajeService.buscarPorCorrelationKey("key-1")).thenReturn(List.of(mensaje));

        assertThat(mensajeController.throwMessage(mensaje).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(mensajeController.catchMessage(mensaje).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(mensajeController.listarPorProceso(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mensajeController.obtenerPorId(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mensajeController.correlateMessages("key-1").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mensajeController.buscarPorCorrelationKey("key-1").getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void notificacion_controller_cubre_rama_enviado_true_false() {
        NotificacionRequestDTO request = new NotificacionRequestDTO();
        NotificacionResponseDTO ok = new NotificacionResponseDTO(true, "EMAIL", "a@b.com", "ok");
        NotificacionResponseDTO error = new NotificacionResponseDTO(false, "EMAIL", "a@b.com", "error");

        when(notificacionService.enviar(request)).thenReturn(ok).thenReturn(error);
        when(notificacionService.enviarEmail("a@b.com", "asunto", "cuerpo")).thenReturn(ok);
        when(notificacionService.enviarWebhook("https://x.com", "{}")) .thenReturn(ok);

        ResponseEntity<ApiResponse<NotificacionResponseDTO>> r1 = notificacionController.enviar(request);
        ResponseEntity<ApiResponse<NotificacionResponseDTO>> r2 = notificacionController.enviar(request);

        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r1.getBody()).isNotNull();
        assertThat(r2.getBody()).isNotNull();
        assertThat(r1.getBody().isSuccess()).isTrue();
        assertThat(r2.getBody().isSuccess()).isFalse();

        assertThat(notificacionController.enviarEmail("a@b.com", "asunto", "cuerpo").getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(notificacionController.enviarWebhook("https://x.com", "{}").getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void auth_controller_login_ok() {
        AuthRequestDTO request = new AuthRequestDTO();
        AuthResponseDTO response = new AuthResponseDTO();
        when(authService.login(request)).thenReturn(response);

        ResponseEntity<ApiResponse<AuthResponseDTO>> result = authController.login(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData()).isEqualTo(response);
    }
}
