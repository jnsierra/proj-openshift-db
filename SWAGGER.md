# Documentación de API con Swagger/OpenAPI

Este proyecto utiliza **SmallRye OpenAPI** (la implementación de Quarkus) para documentar la API REST automáticamente mediante el estándar OpenAPI 3.0.

## Acceso a la Documentación

Una vez que la aplicación esté ejecutándose, puedes acceder a la documentación de la API en las siguientes URLs:

### Swagger UI (Interfaz Interactiva)
```
http://localhost:8080/swagger-ui
```

La interfaz de Swagger UI te permite:
- Ver todos los endpoints disponibles
- Leer la documentación de cada endpoint
- Probar los endpoints directamente desde el navegador
- Ver los esquemas de datos (DTOs)
- Ver los códigos de respuesta posibles

### OpenAPI Schema (JSON)
```
http://localhost:8080/q/openapi
```

Este endpoint devuelve la especificación completa de OpenAPI en formato JSON, útil para:
- Generar clientes automáticamente
- Importar en herramientas como Postman
- Integración con sistemas externos

### OpenAPI Schema (YAML)
```
http://localhost:8080/q/openapi?format=yaml
```

Mismo contenido que el endpoint anterior pero en formato YAML.

## Características Implementadas

### 1. Información General de la API
La API está documentada con:
- **Título:** API de Gestión de Personas
- **Versión:** 1.0.0
- **Descripción:** API REST para gestionar información de personas con arquitectura hexagonal
- **Contacto:** FISA Tech Team (contacto@fisa.com.ec)
- **Licencia:** Apache 2.0

### 2. Endpoints Documentados

Todos los endpoints de `/api/personas` están completamente documentados:

#### GET /api/personas
- **Resumen:** Listar todas las personas
- **Respuestas:** 200 (Listado exitoso)

#### GET /api/personas/{id}
- **Resumen:** Obtener persona por ID
- **Parámetros:** ID de la persona (requerido)
- **Respuestas:**
  - 200 (Persona encontrada)
  - 404 (Persona no encontrada)

#### POST /api/personas
- **Resumen:** Crear nueva persona
- **Body:** PersonaDTO (nombre, apellido, edad)
- **Respuestas:**
  - 201 (Persona creada)
  - 400 (Datos inválidos)

#### PUT /api/personas/{id}
- **Resumen:** Actualizar persona existente
- **Parámetros:** ID de la persona (requerido)
- **Body:** PersonaDTO (nombre, apellido, edad)
- **Respuestas:**
  - 200 (Persona actualizada)
  - 400 (Datos inválidos)
  - 404 (Persona no encontrada)

#### DELETE /api/personas/{id}
- **Resumen:** Eliminar persona
- **Parámetros:** ID de la persona (requerido)
- **Respuestas:**
  - 204 (Persona eliminada)
  - 404 (Persona no encontrada)

### 3. Esquemas de Datos Documentados

#### PersonaDTO
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "edad": 30
}
```

Campos:
- **id:** Identificador único (solo lectura, generado automáticamente)
- **nombre:** Nombre de la persona (requerido, 1-100 caracteres)
- **apellido:** Apellido de la persona (requerido, 1-100 caracteres)
- **edad:** Edad en años (requerido, mínimo 0)

#### ErrorResponse
```json
{
  "message": "El nombre es requerido"
}
```

Usado para respuestas de error 400 (Bad Request).

## Configuración

La configuración de OpenAPI se encuentra en `src/main/resources/application.properties`:

```properties
# Habilitar Swagger UI siempre (incluso en producción si es necesario)
quarkus.swagger-ui.always-include=true

# Ruta personalizada para Swagger UI
quarkus.swagger-ui.path=/swagger-ui

# Información de la API
mp.openapi.extensions.smallrye.info.title=API de Gestión de Personas
mp.openapi.extensions.smallrye.info.version=1.0.0
mp.openapi.extensions.smallrye.info.description=API REST para gestionar información de personas con arquitectura hexagonal
mp.openapi.extensions.smallrye.info.contact.email=contacto@fisa.com.ec
mp.openapi.extensions.smallrye.info.contact.name=FISA Tech Team
mp.openapi.extensions.smallrye.info.license.name=Apache 2.0
mp.openapi.extensions.smallrye.info.license.url=https://www.apache.org/licenses/LICENSE-2.0.html
```

## Anotaciones Utilizadas

### En PersonaResource.java

- `@Tag`: Agrupa endpoints bajo una categoría
- `@Operation`: Describe el propósito de cada endpoint
- `@APIResponses` / `@APIResponse`: Documenta los códigos de respuesta HTTP
- `@Parameter`: Describe parámetros de path o query
- `@Schema`: Documenta el esquema de datos en respuestas

### En PersonaDTO.java

- `@Schema`: Documenta la clase y cada campo
  - `description`: Descripción del campo
  - `example`: Valor de ejemplo
  - `required`: Si el campo es requerido
  - `readOnly`: Si el campo es solo de lectura
  - `minLength` / `maxLength`: Validaciones de longitud
  - `minimum`: Valor mínimo para números

## Uso desde Swagger UI

1. Inicia la aplicación:
   ```bash
   docker compose -f src/main/resources/compose/docker-compose.yml up -d
   ./mvnw quarkus:dev
   ```

2. Abre tu navegador en: `http://localhost:8080/swagger-ui`

3. Explora los endpoints disponibles

4. Para probar un endpoint:
   - Haz clic en el endpoint que deseas probar
   - Haz clic en "Try it out"
   - Completa los parámetros necesarios
   - Haz clic en "Execute"
   - Verás la respuesta del servidor abajo

## Ejemplo de Prueba

### Crear una Persona
1. Ve a `POST /api/personas`
2. Haz clic en "Try it out"
3. En el body, ingresa:
   ```json
   {
     "nombre": "María",
     "apellido": "García",
     "edad": 25
   }
   ```
4. Haz clic en "Execute"
5. Verás la respuesta con código 201 y la persona creada con su ID

### Listar Personas
1. Ve a `GET /api/personas`
2. Haz clic en "Try it out"
3. Haz clic en "Execute"
4. Verás la lista de todas las personas registradas

## Importar en Postman

1. Abre Postman
2. Ve a File → Import
3. Selecciona "Link"
4. Ingresa: `http://localhost:8080/q/openapi`
5. Haz clic en "Continue" y luego "Import"
6. Todos los endpoints estarán disponibles en una colección

## Desactivar Swagger UI en Producción

Si deseas desactivar Swagger UI en producción, modifica `application.properties`:

```properties
# Solo incluir en modo desarrollo
quarkus.swagger-ui.always-include=false
```

Con esta configuración, Swagger UI solo estará disponible en modo desarrollo (`mvn quarkus:dev`).

## Referencias

- [Documentación de Quarkus OpenAPI](https://quarkus.io/guides/openapi-swaggerui)
- [Especificación OpenAPI 3.0](https://swagger.io/specification/)
- [MicroProfile OpenAPI](https://github.com/eclipse/microprofile-open-api)
