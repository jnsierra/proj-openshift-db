# proj-openshift-db

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Configuración del Entorno

Este proyecto utiliza variables de entorno para la configuración de la base de datos.

### Configurar el archivo .env

1. Copia el archivo de ejemplo:
```shell script
cp .env.example .env
```

2. Edita el archivo `.env` con tus credenciales:
```properties
# Database Configuration
POSTGRES_USER=openshift
POSTGRES_PASSWORD=openshift
POSTGRES_DB=proj-openshift
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# Application Configuration
APP_PORT=8080
```

### Variables de Entorno Disponibles

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `POSTGRES_USER` | Usuario de PostgreSQL | `openshift` |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL | `openshift` |
| `POSTGRES_DB` | Nombre de la base de datos | `proj-openshift` |
| `POSTGRES_HOST` | Host de PostgreSQL | `localhost` |
| `POSTGRES_PORT` | Puerto de PostgreSQL | `5432` |

> **Nota:** El archivo `.env` está en `.gitignore` y no se commitea al repositorio por seguridad.

### Iniciar la Base de Datos

```shell script
docker compose -f src/main/resources/compose/docker-compose.yml up -d
```

El contenedor de PostgreSQL utilizará automáticamente las variables definidas en el archivo `.env`.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Documentación de la API

Este proyecto incluye documentación interactiva de la API REST usando **Swagger UI / OpenAPI**.

### Acceso a la Documentación

Una vez que la aplicación esté ejecutándose, puedes acceder a:

#### Swagger UI (Interfaz Interactiva)
```
http://localhost:8080/swagger-ui
```

Desde Swagger UI puedes:
- Ver todos los endpoints disponibles
- Probar los endpoints directamente desde el navegador
- Ver los esquemas de datos (DTOs)
- Ver ejemplos de request/response

#### OpenAPI Schema
```
http://localhost:8080/q/openapi        # Formato JSON
http://localhost:8080/q/openapi?format=yaml  # Formato YAML
```

### Endpoints Disponibles

- `GET /api/personas` - Listar todas las personas
- `GET /api/personas/{id}` - Obtener persona por ID
- `POST /api/personas` - Crear nueva persona
- `PUT /api/personas/{id}` - Actualizar persona
- `DELETE /api/personas/{id}` - Eliminar persona

Para más detalles, consulta el archivo [SWAGGER.md](SWAGGER.md).

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/proj-openshift-db-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
