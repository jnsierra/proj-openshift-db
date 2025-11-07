# Guía de Docker

Esta guía explica cómo dockerizar y ejecutar el microservicio usando Docker y Docker Compose.

## Contenido

- [Dockerfile](#dockerfile)
- [Docker Compose](#docker-compose)
- [Construcción de la Imagen](#construcción-de-la-imagen)
- [Ejecución con Docker](#ejecución-con-docker)
- [Ejecución con Docker Compose](#ejecución-con-docker-compose)
- [Variables de Entorno](#variables-de-entorno)
- [Troubleshooting](#troubleshooting)

## Dockerfile

El proyecto incluye un `Dockerfile` multi-stage optimizado que:

### Stage 1: Build
- Utiliza Maven con JDK 21 para compilar la aplicación
- Implementa cache de dependencias para builds más rápidos
- Genera el artefacto ejecutable de Quarkus

### Stage 2: Runtime
- Usa una imagen JRE Alpine ligera (solo 50-80 MB)
- Incluye healthcheck para monitoreo
- Ejecuta la aplicación como usuario no-root por seguridad
- Expone el puerto 8080

### Características de Seguridad

- Usuario no-root (`quarkus:quarkus`)
- Imagen base Alpine (menor superficie de ataque)
- Health checks integrados
- No incluye herramientas de build en producción

## Docker Compose

El proyecto incluye dos archivos de Docker Compose:

### 1. `docker-compose.yml` (Raíz del proyecto)
Orquesta toda la aplicación (PostgreSQL + Microservicio):

```yaml
services:
  postgres:    # Base de datos PostgreSQL
  app:         # Microservicio Quarkus
```

### 2. `src/main/resources/compose/docker-compose.yml`
Solo PostgreSQL (para desarrollo local):

```yaml
services:
  postgres:    # Solo base de datos
```

## Construcción de la Imagen

### Construir la imagen manualmente

```bash
# Construir la imagen con tag
docker build -t jesusnicolassierra/proj-openshift-db:1.0.0 .

# O con tag latest
docker build -t jesusnicolassierra/proj-openshift-db:latest .
```

### Verificar la imagen creada

```bash
docker images | grep proj-openshift-db
```

### Construir con Docker Compose

```bash
# Construir sin cache
docker compose build --no-cache

# Construir con cache
docker compose build
```

## Ejecución con Docker

### 1. Configurar Variables de Entorno

Asegúrate de tener el archivo `.env`:

```bash
cp .env.example .env
```

### 2. Iniciar PostgreSQL

```bash
docker compose -f src/main/resources/compose/docker-compose.yml up -d
```

### 3. Ejecutar la Aplicación

#### Opción A: Usando la imagen construida

```bash
docker run -d \
  --name proj-openshift-app \
  --network host \
  --env-file .env \
  -p 8080:8080 \
  proj-openshift-db:latest
```

#### Opción B: Con variables individuales

```bash
docker run -d \
  --name proj-openshift-app \
  -e POSTGRES_USER=openshift \
  -e POSTGRES_PASSWORD=openshift \
  -e POSTGRES_DB=proj-openshift \
  -e POSTGRES_HOST=localhost \
  -e POSTGRES_PORT=5432 \
  -p 8080:8080 \
  proj-openshift-db:latest
```

### 4. Ver Logs

```bash
# Logs en tiempo real
docker logs -f proj-openshift-app

# Últimas 100 líneas
docker logs --tail 100 proj-openshift-app
```

### 5. Detener y Eliminar

```bash
# Detener
docker stop proj-openshift-app

# Eliminar
docker rm proj-openshift-app
```

## Ejecución con Docker Compose

Docker Compose simplifica la orquestación de múltiples contenedores.

### Iniciar Todos los Servicios

```bash
# Construir y levantar en segundo plano
docker compose up -d --build

# O sin rebuild
docker compose up -d
```

### Ver Estado de los Servicios

```bash
docker compose ps
```

Salida esperada:
```
NAME                   IMAGE                      STATUS        PORTS
postgres-openshift     postgres:18.0-alpine3.22   Up (healthy)  0.0.0.0:5432->5432/tcp
proj-openshift-app     proj-openshift-db          Up (healthy)  0.0.0.0:8080->8080/tcp
```

### Ver Logs

```bash
# Todos los servicios
docker compose logs -f

# Solo la aplicación
docker compose logs -f app

# Solo PostgreSQL
docker compose logs -f postgres
```

### Detener Servicios

```bash
# Detener sin eliminar
docker compose stop

# Detener y eliminar contenedores
docker compose down

# Detener, eliminar contenedores y volúmenes
docker compose down -v
```

### Reiniciar Servicios

```bash
# Reiniciar todos
docker compose restart

# Reiniciar solo la app
docker compose restart app
```

### Reconstruir y Reiniciar

```bash
# Reconstruir la aplicación
docker compose build app

# Recrear solo el servicio app
docker compose up -d --force-recreate app
```

## Variables de Entorno

### Variables Requeridas

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `POSTGRES_USER` | Usuario de PostgreSQL | `openshift` |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL | `openshift` |
| `POSTGRES_DB` | Nombre de la base de datos | `proj-openshift` |
| `POSTGRES_HOST` | Host de PostgreSQL | `localhost` (Docker: `postgres`) |
| `POSTGRES_PORT` | Puerto de PostgreSQL | `5432` |
| `APP_PORT` | Puerto de la aplicación | `8080` |

### Archivo .env

Docker Compose lee automáticamente el archivo `.env`:

```properties
POSTGRES_USER=openshift
POSTGRES_PASSWORD=openshift
POSTGRES_DB=proj-openshift
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
APP_PORT=8080
```

**Importante:** Cuando usas Docker Compose, `POSTGRES_HOST` debe ser `postgres` (nombre del servicio).

## Health Checks

### PostgreSQL Health Check

```bash
docker exec postgres-openshift pg_isready -U openshift
```

### Aplicación Health Check

```bash
# Liveness
curl http://localhost:8080/q/health/live

# Readiness
curl http://localhost:8080/q/health/ready

# Full health
curl http://localhost:8080/q/health
```

## Networking

### Red en Docker Compose

Docker Compose crea automáticamente una red `app-network`:

- PostgreSQL: `postgres:5432` (interno)
- Aplicación: `app:8080` (interno)

Los servicios se comunican por nombre:
```
app → postgres:5432
```

### Acceso desde Host

- PostgreSQL: `localhost:5432`
- Aplicación: `localhost:8080`

## Volúmenes

### Volumen de PostgreSQL

```bash
# Listar volúmenes
docker volume ls | grep db-data

# Inspeccionar volumen
docker volume inspect proj-openshift-db_db-data

# Eliminar volumen (¡cuidado! elimina todos los datos)
docker compose down -v
```

### Backup del Volumen

```bash
# Hacer backup
docker run --rm \
  -v proj-openshift-db_db-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/db-backup.tar.gz -C /data .

# Restaurar backup
docker run --rm \
  -v proj-openshift-db_db-data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/db-backup.tar.gz -C /data
```

## Optimización de Imagen

### Tamaños de Imagen

| Etapa | Tamaño Aproximado |
|-------|-------------------|
| Build stage (Maven + JDK) | ~800 MB |
| Runtime stage (JRE Alpine) | ~250 MB |
| Imagen final | ~250 MB |

### Multi-stage Build

El Dockerfile usa multi-stage build para:
1. Reducir el tamaño de la imagen final
2. No incluir herramientas de desarrollo en producción
3. Mejorar la seguridad

### Cache de Capas

Para builds más rápidos:
1. Primero copia `pom.xml` y descarga dependencias
2. Luego copia el código fuente
3. Las dependencias se cachean si `pom.xml` no cambia

## Troubleshooting

### La aplicación no se conecta a PostgreSQL

**Síntomas:**
```
Connection refused: postgres:5432
```

**Soluciones:**
1. Verifica que `POSTGRES_HOST=postgres` en `.env`
2. Verifica que PostgreSQL esté corriendo:
   ```bash
   docker compose ps postgres
   ```
3. Revisa los logs de PostgreSQL:
   ```bash
   docker compose logs postgres
   ```

### Build falla por falta de memoria

**Síntomas:**
```
java.lang.OutOfMemoryError
```

**Solución:**
Aumenta la memoria de Docker:
- Docker Desktop → Settings → Resources → Memory: 4-8 GB

### Permisos denegados

**Síntomas:**
```
Permission denied
```

**Solución:**
La aplicación corre como usuario `quarkus` (no-root). Verifica permisos de archivos montados.

### Puerto ya en uso

**Síntomas:**
```
Error: bind: address already in use
```

**Solución:**
```bash
# Encontrar proceso usando el puerto
lsof -i :8080

# Cambiar puerto en .env
APP_PORT=8081
```

### Healthcheck falla

**Síntomas:**
```
unhealthy
```

**Solución:**
1. Aumenta `start_period` en healthcheck
2. Verifica que la aplicación esté respondiendo:
   ```bash
   docker compose exec app curl -f http://localhost:8080/q/health/live
   ```

### Cache de dependencias no funciona

**Solución:**
```bash
# Limpiar cache de Docker
docker builder prune

# Rebuild sin cache
docker compose build --no-cache
```

## Comandos Útiles

### Inspeccionar Contenedores

```bash
# Ver detalles del contenedor
docker inspect proj-openshift-app

# Ver variables de entorno
docker inspect proj-openshift-app | grep -A 20 Env

# Ver red
docker inspect proj-openshift-app | grep -A 10 Network
```

### Ejecutar Comandos en Contenedor

```bash
# Shell interactivo
docker compose exec app sh

# Ejecutar comando específico
docker compose exec app curl http://localhost:8080/q/health
```

### Limpiar Recursos

```bash
# Eliminar contenedores detenidos
docker container prune

# Eliminar imágenes sin usar
docker image prune

# Eliminar volúmenes sin usar
docker volume prune

# Limpiar todo (¡cuidado!)
docker system prune -a --volumes
```

## Despliegue en Producción

### Recomendaciones

1. **Usa tags específicos:**
   ```bash
   docker build -t proj-openshift-db:1.0.0 .
   ```

2. **No uses `latest` en producción**

3. **Configura límites de recursos:**
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '1'
         memory: 512M
       reservations:
         cpus: '0.5'
         memory: 256M
   ```

4. **Usa secrets para credenciales:**
   ```yaml
   secrets:
     postgres_password:
       file: ./secrets/postgres_password.txt
   ```

5. **Habilita logs externos:**
   ```yaml
   logging:
     driver: "json-file"
     options:
       max-size: "10m"
       max-file: "3"
   ```

## Registry y Distribución

### Subir a Docker Hub

```bash
# Tag con tu usuario
docker tag proj-openshift-db:1.0.0 tu-usuario/proj-openshift-db:1.0.0

# Login
docker login

# Push
docker push tu-usuario/proj-openshift-db:1.0.0
```

### Subir a Registry Privado

```bash
# Tag con registry privado
docker tag proj-openshift-db:1.0.0 registry.ejemplo.com/proj-openshift-db:1.0.0

# Push
docker push registry.ejemplo.com/proj-openshift-db:1.0.0
```

## Referencias

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Quarkus Container Images](https://quarkus.io/guides/container-image)
- [Best Practices for Writing Dockerfiles](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
