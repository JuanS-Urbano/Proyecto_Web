# JMeter - Proyecto Web

Este paquete incluye un plan base de rendimiento con 3 escenarios:

- Smoke: validacion rapida de endpoints criticos.
- Carga: concurrencia media sostenida.
- Estres: concurrencia alta para observar degradacion.

Archivo principal:

- ProyectoWeb_API_TestPlan.jmx

## Requisitos

- JMeter 5.6.x o superior.
- API corriendo en localhost:8080 (o ajustar variables del Test Plan).

## Variables globales del plan

Puedes editarlas en Test Plan -> Variables Globales:

- base_protocol (default: http)
- base_host (default: localhost)
- base_port (default: 8080)
- conn_timeout (default: 5000)
- resp_timeout (default: 5000)

## Escenarios incluidos

1. 01 - Smoke
- 1 usuario
- 1 iteracion
- Endpoints: /actuator/health y /api/v1/empresas
- Assertions: HTTP 200 y tiempo maximo

2. 02 - Carga
- 30 usuarios
- ramp-up 60s
- 10 iteraciones por hilo
- Endpoint: /api/v1/empresas
- Assertions: HTTP 200 y duracion max 4000ms

3. 03 - Estres
- 80 usuarios
- ramp-up 80s
- 5 iteraciones por hilo
- Endpoint: /actuator/health
- Assertions: HTTP 200 y duracion max 3000ms

## Ejecucion desde GUI

1. Abrir JMeter.
2. File -> Open -> jmeter/ProyectoWeb_API_TestPlan.jmx.
3. Ejecutar con Start.
4. Revisar Summary Report.

## Ejecucion por consola (no GUI)

Smoke:

jmeter -n -t jmeter/ProyectoWeb_API_TestPlan.jmx -Jjmeterengine.force.system.exit=true -l jmeter/results/smoke.jtl -e -o jmeter/results/smoke-report

Carga:

jmeter -n -t jmeter/ProyectoWeb_API_TestPlan.jmx -Jjmeterengine.force.system.exit=true -l jmeter/results/load.jtl -e -o jmeter/results/load-report

Estres:

jmeter -n -t jmeter/ProyectoWeb_API_TestPlan.jmx -Jjmeterengine.force.system.exit=true -l jmeter/results/stress.jtl -e -o jmeter/results/stress-report

Nota: el plan trae los 3 Thread Group. Si quieres ejecutar solo uno, deshabilita los otros desde GUI o duplicamos el plan por escenario.

## Criterio sugerido para entregar

- Error % menor a 1%
- p95 menor a 2000 ms en carga
- sin errores de assertion

Si quieres, te dejo enseguida una version avanzada con:

- CSV Data Set para ids reales
- autenticacion (header token)
- pruebas de POST/PUT con payloads dinamicos
