package com.grupo1.editorprocesos.config;

import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import com.grupo1.editorprocesos.model.entity.bpmn.Gateway;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.entity.process.RolProceso;
import com.grupo1.editorprocesos.model.enums.*;
import com.grupo1.editorprocesos.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final PoolRepository poolRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolProcesoRepository rolProcesoRepository;
    private final ProcesoRepository procesoRepository;
    private final LaneRepository laneRepository;
    private final ActividadRepository actividadRepository;
    private final GatewayRepository gatewayRepository;
    private final ArcoRepository arcoRepository;
    private final MensajeRepository mensajeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (empresaRepository.count() > 0) {
            log.info("DataSeeder: datos ya existen, omitiendo seed.");
            return;
        }

        log.info("DataSeeder: iniciando carga de datos demo...");

        Empresa techcorp = crearEmpresa("TechCorp SA", "900111222-3", "contacto@techcorp.com");
        Empresa logistica = crearEmpresa("Logística Plus", "800333444-5", "contacto@logistica.com");

        Pool poolTech = crearPool("Pool Principal - TechCorp SA", techcorp);
        Pool poolLog = crearPool("Pool Principal - Logística Plus", logistica);

        crearUsuario("admin@techcorp.com",    "900111222-3", RolSistema.ADMIN_EMPRESA, techcorp);
        crearUsuario("editor@techcorp.com",   "editor123",   RolSistema.EDITOR,        techcorp);
        crearUsuario("lector@techcorp.com",   "lector123",   RolSistema.LECTOR,        techcorp);

        crearUsuario("admin@logistica.com",   "800333444-5", RolSistema.ADMIN_EMPRESA, logistica);
        crearUsuario("editor@logistica.com",  "editor123",   RolSistema.EDITOR,        logistica);
        crearUsuario("lector@logistica.com",  "lector123",   RolSistema.LECTOR,        logistica);

        RolProceso analista  = crearRol("Analista",   "Analiza y documenta los procesos de negocio", techcorp);
        RolProceso supervisor = crearRol("Supervisor", "Supervisa la ejecución de los procesos",       techcorp);
        RolProceso analistaLog  = crearRol("Analista",   "Analiza y documenta los procesos de negocio", logistica);
        RolProceso supervisorLog = crearRol("Supervisor", "Supervisa la ejecución de los procesos",     logistica);

        Proceso proc1 = crearProceso("Proceso de Ventas", "Gestión completa del ciclo de ventas", "Ventas",
                EstadoProceso.PUBLICADO, false, poolTech);
        Proceso proc2 = crearProceso("Onboarding de Clientes", "Incorporación de nuevos clientes al sistema", "RRHH",
                EstadoProceso.BORRADOR, true, poolTech);
        crearProceso("Control de Calidad", "Proceso de revisión y control de calidad", "Operaciones",
                EstadoProceso.INACTIVO, false, poolTech);

        Proceso proc4 = crearProceso("Gestión de Inventario", "Control y seguimiento del inventario", "Logística",
                EstadoProceso.PUBLICADO, false, poolLog);
        Proceso proc5 = crearProceso("Despacho de Pedidos", "Preparación y envío de pedidos a clientes", "Operaciones",
                EstadoProceso.BORRADOR, false, poolLog);
        crearProceso("Devoluciones", "Proceso de gestión de devoluciones", "Atención al Cliente",
                EstadoProceso.INACTIVO, false, poolLog);

        poblarProceso(proc1, analista,    supervisor);
        poblarProceso(proc2, analista,    analista);
        poblarProceso(proc4, analistaLog, supervisorLog);

        crearParMensajes(proc1, proc2, "demo-key-001", "Notificación de Venta Cerrada");
        crearParMensajes(proc4, proc5, "demo-key-002", "Solicitud de Despacho");

        log.info("DataSeeder: seed completado exitosamente.");
        log.info("=== CREDENCIALES DEMO ===");
        log.info("Admin TechCorp:    admin@techcorp.com  / 900111222-3");
        log.info("Editor TechCorp:   editor@techcorp.com / editor123");
        log.info("Lector TechCorp:   lector@techcorp.com / lector123");
        log.info("Admin Logística:   admin@logistica.com / 800333444-5");
        log.info("Editor Logística:  editor@logistica.com / editor123");
        log.info("========================");
    }

    private Empresa crearEmpresa(String nombre, String nit, String correo) {
        Empresa e = new Empresa();
        e.setNombre(nombre);
        e.setNit(nit);
        e.setCorreoContacto(correo);
        return empresaRepository.save(e);
    }

    private Pool crearPool(String nombre, Empresa empresa) {
        Pool p = new Pool();
        p.setNombre(nombre);
        p.setEmpresa(empresa);
        return poolRepository.save(p);
    }

    private void crearUsuario(String email, String password, RolSistema rol, Empresa empresa) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setRolSistema(rol);
        u.setEmpresa(empresa);
        u.setIsActivo(true);
        usuarioRepository.save(u);
    }

    private RolProceso crearRol(String nombre, String descripcion, Empresa empresa) {
        RolProceso r = new RolProceso();
        r.setNombre(nombre);
        r.setDescripcion(descripcion);
        r.setEmpresa(empresa);
        return rolProcesoRepository.save(r);
    }

    private Proceso crearProceso(String nombre, String descripcion, String categoria,
                                  EstadoProceso estado, boolean compartido, Pool pool) {
        Proceso p = new Proceso();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setCategoria(categoria);
        p.setEstado(estado);
        p.setConfiguracionCompartido(compartido);
        p.setPool(pool);
        return procesoRepository.save(p);
    }

    private void poblarProceso(Proceso proceso, RolProceso rolLane1, RolProceso rolLane2) {
        Lane lane1 = new Lane();
        lane1.setNombre("Iniciador");
        lane1.setDescripcion("Responsable de iniciar el proceso");
        lane1.setOrden(1);
        lane1.setPosicionX(0.0);
        lane1.setPosicionY(0.0);
        lane1.setProceso(proceso);
        lane1.setRolProceso(rolLane1);
        laneRepository.save(lane1);

        Lane lane2 = new Lane();
        lane2.setNombre("Aprobador");
        lane2.setDescripcion("Responsable de aprobar o rechazar");
        lane2.setOrden(2);
        lane2.setPosicionX(0.0);
        lane2.setPosicionY(120.0);
        lane2.setProceso(proceso);
        lane2.setRolProceso(rolLane2);
        laneRepository.save(lane2);

        Actividad inicio = new Actividad();
        inicio.setNombre("Inicio del Proceso");
        inicio.setTipoActividad(TipoActividad.MANUAL);
        inicio.setPosicionX(100.0);
        inicio.setPosicionY(50.0);
        inicio.setProceso(proceso);
        inicio.setLane(lane1);
        actividadRepository.save(inicio);

        Actividad tarea = new Actividad();
        tarea.setNombre("Ejecutar Tarea Principal");
        tarea.setTipoActividad(TipoActividad.USUARIO);
        tarea.setPosicionX(300.0);
        tarea.setPosicionY(50.0);
        tarea.setProceso(proceso);
        tarea.setLane(lane1);
        actividadRepository.save(tarea);

        Gateway gateway = new Gateway();
        gateway.setNombre("¿Aprobado?");
        gateway.setTipoGateway(TipoGateway.EXCLUSIVO);
        gateway.setPosicionX(500.0);
        gateway.setPosicionY(50.0);
        gateway.setProceso(proceso);
        gatewayRepository.save(gateway);

        Actividad aprobacion = new Actividad();
        aprobacion.setNombre("Registrar Aprobación");
        aprobacion.setTipoActividad(TipoActividad.USUARIO);
        aprobacion.setPosicionX(700.0);
        aprobacion.setPosicionY(30.0);
        aprobacion.setProceso(proceso);
        aprobacion.setLane(lane2);
        actividadRepository.save(aprobacion);

        Actividad rechazo = new Actividad();
        rechazo.setNombre("Notificar Rechazo");
        rechazo.setTipoActividad(TipoActividad.ENVIO);
        rechazo.setPosicionX(700.0);
        rechazo.setPosicionY(120.0);
        rechazo.setProceso(proceso);
        rechazo.setLane(lane2);
        actividadRepository.save(rechazo);

        crearArco(String.valueOf(inicio.getId()), String.valueOf(tarea.getId()), proceso);
        crearArco(String.valueOf(tarea.getId()), String.valueOf(gateway.getId()), proceso);
        crearArco(String.valueOf(gateway.getId()), String.valueOf(aprobacion.getId()), proceso);
        crearArco(String.valueOf(gateway.getId()), String.valueOf(rechazo.getId()), proceso);
    }

    private void crearArco(String origenId, String destinoId, Proceso proceso) {
        Arco a = new Arco();
        a.setOrigenId(origenId);
        a.setDestinoId(destinoId);
        a.setProceso(proceso);
        arcoRepository.save(a);
    }

    private void crearParMensajes(Proceso procesoOrigen, Proceso procesoDestino,
                                   String correlationKey, String nombre) {
        Mensaje throwMsg = new Mensaje();
        throwMsg.setNombre(nombre + " (THROW)");
        throwMsg.setTipo(TipoMensaje.THROW);
        throwMsg.setEstado(EstadoMensaje.PENDIENTE);
        throwMsg.setCorrelationKey(correlationKey);
        throwMsg.setProceso(procesoOrigen);
        throwMsg.setProcesoDestinoId(procesoDestino.getId());
        throwMsg.setPayloadJson("{\"evento\": \"" + nombre + "\", \"origen\": \"" + procesoOrigen.getNombre() + "\"}");
        mensajeRepository.save(throwMsg);

        Mensaje catchMsg = new Mensaje();
        catchMsg.setNombre(nombre + " (CATCH)");
        catchMsg.setTipo(TipoMensaje.CATCH);
        catchMsg.setEstado(EstadoMensaje.PENDIENTE);
        catchMsg.setCorrelationKey(correlationKey);
        catchMsg.setProceso(procesoDestino);
        catchMsg.setProcesoDestinoId(procesoDestino.getId());
        catchMsg.setPayloadJson("{\"evento\": \"" + nombre + "\", \"destino\": \"" + procesoDestino.getNombre() + "\"}");
        mensajeRepository.save(catchMsg);
    }
}
