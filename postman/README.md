# Postman - Proyecto Web

Archivos creados:

- Proyecto_Web_API.postman_collection.json
- Proyecto_Web_Environment.postman_environment.json

## Importar en Postman

1. Abrir Postman.
2. Import -> Upload Files.
3. Importar la coleccion y el environment de la carpeta postman.
4. Seleccionar el environment "Proyecto Web Local".
5. Verificar variable baseUrl = http://localhost:8080.

## Ejecutar tests en Postman

1. Abrir la coleccion "Proyecto Web API".
2. Click en Run collection.
3. Ejecutar todos los requests.
4. Revisar pestaña Test Results.

## Que valida automaticamente

- Status code esperado (200 o 200/404 segun endpoint).
- Tiempo de respuesta maximo.
- Estructura de respuesta (wrapper ApiResponse y campos clave).

## Ejecutar por consola con Newman (opcional)

Instalar newman (una sola vez):

npm install -g newman

Ejecutar:

newman run postman/Proyecto_Web_API.postman_collection.json -e postman/Proyecto_Web_Environment.postman_environment.json

Generar reporte html (opcional):

npm install -g newman-reporter-htmlextra
newman run postman/Proyecto_Web_API.postman_collection.json -e postman/Proyecto_Web_Environment.postman_environment.json -r cli,htmlextra --reporter-htmlextra-export postman/newman-report.html
