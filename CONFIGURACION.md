# Configuración del Proyecto

Este documento describe cómo configurar el proyecto utilizando variables de entorno.

## Variables de Entorno

El proyecto utiliza un archivo `.env` para externalizar la configuración de la base de datos y otros parámetros sensibles.

### Archivo .env

El archivo `.env` debe estar ubicado en la raíz del proyecto:

```
proj-openshift-db/
├── .env                    # Tu configuración local (no se commitea)
├── .env.example           # Plantilla de ejemplo
├── src/
└── ...
```

### Configuración Inicial

1. **Copia el archivo de ejemplo:**
   ```bash
   cp .env.example .env
   ```

2. **Personaliza las variables según tu entorno:**
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

### Variables Disponibles

#### Variables de Base de Datos

| Variable | Descripción | Valor por Defecto | Requerida |
|----------|-------------|-------------------|-----------|
| `POSTGRES_USER` | Usuario de PostgreSQL | `openshift` | Sí |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL | `openshift` | Sí |
| `POSTGRES_DB` | Nombre de la base de datos | `proj-openshift` | Sí |
| `POSTGRES_HOST` | Host de PostgreSQL (localhost o nombre del contenedor) | `localhost` | Sí |
| `POSTGRES_PORT` | Puerto de PostgreSQL | `5432` | Sí |

#### Variables de Aplicación

| Variable | Descripción | Valor por Defecto | Requerida |
|----------|-------------|-------------------|-----------|
| `APP_PORT` | Puerto de la aplicación Quarkus | `8080` | No |

## Uso en Diferentes Entornos

### Desarrollo Local

Para desarrollo local con Docker:

```properties
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
```

### Docker Compose

El archivo `docker-compose.yml` utiliza automáticamente las variables del archivo `.env`:

```bash
docker compose -f src/main/resources/compose/docker-compose.yml up -d
```

### Contenedor de Aplicación

Si ejecutas la aplicación Quarkus en un contenedor junto con PostgreSQL:

```properties
POSTGRES_HOST=postgres  # Nombre del servicio en docker-compose
POSTGRES_PORT=5432
```

### Producción

En producción, las variables de entorno se pueden configurar de diferentes formas:

#### OpenShift/Kubernetes

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
type: Opaque
stringData:
  POSTGRES_USER: openshift
  POSTGRES_PASSWORD: password_seguro
  POSTGRES_DB: proj-openshift
  POSTGRES_HOST: postgres-service
  POSTGRES_PORT: "5432"
```

Referencia en el Deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: proj-openshift-db
spec:
  template:
    spec:
      containers:
      - name: app
        image: proj-openshift-db:latest
        envFrom:
        - secretRef:
            name: postgres-secret
```

#### Variables de Sistema

También puedes exportar las variables directamente:

```bash
export POSTGRES_USER=openshift
export POSTGRES_PASSWORD=mi_password
export POSTGRES_DB=proj-openshift
export POSTGRES_HOST=db.example.com
export POSTGRES_PORT=5432

./mvnw quarkus:dev
```

## Cómo Funcionan las Variables

### En application.properties

Las variables de entorno se referencian con la sintaxis `${VARIABLE:default}`:

```properties
quarkus.datasource.username=${POSTGRES_USER:openshift}
quarkus.datasource.password=${POSTGRES_PASSWORD:openshift}
quarkus.datasource.jdbc.url=jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:proj-openshift}
```

Si la variable de entorno existe, se usa su valor. Si no existe, se usa el valor por defecto después de los dos puntos.

### En docker-compose.yml

Docker Compose utiliza la sintaxis `${VARIABLE:-default}`:

```yaml
environment:
  POSTGRES_USER: ${POSTGRES_USER:-openshift}
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-openshift}
  POSTGRES_DB: ${POSTGRES_DB:-proj-openshift}
```

El archivo `.env` se carga automáticamente usando la directiva `env_file`:

```yaml
env_file:
  - ../../../../.env
```

## Seguridad

### Buenas Prácticas

1. **Nunca commitees el archivo .env** - Ya está en `.gitignore`
2. **Usa contraseñas fuertes en producción** - Los valores por defecto son solo para desarrollo
3. **Rota las credenciales regularmente** - Especialmente en producción
4. **Limita el acceso al archivo .env** - Solo lectura para la aplicación
5. **Usa secretos de Kubernetes/OpenShift en producción** - No variables de entorno directas

### Verificar que .env está en .gitignore

```bash
git check-ignore .env
```

Debería retornar:
```
.env
```

Si no está ignorado, agrégalo a `.gitignore`:

```bash
echo ".env" >> .gitignore
```

## Validación de Configuración

Para verificar que las variables están configuradas correctamente:

1. **Verificar variables cargadas en Docker Compose:**
   ```bash
   docker compose -f src/main/resources/compose/docker-compose.yml config
   ```

2. **Verificar conexión a la base de datos:**
   ```bash
   docker compose -f src/main/resources/compose/docker-compose.yml exec postgres psql -U $POSTGRES_USER -d $POSTGRES_DB -c "SELECT version();"
   ```

3. **Logs de la aplicación:**
   ```bash
   ./mvnw quarkus:dev
   ```

   Verifica en los logs que la URL de conexión sea correcta:
   ```
   Hibernate:
   select ... from persona ...
   ```

## Troubleshooting

### Error: Connection refused

**Problema:** La aplicación no puede conectarse a PostgreSQL

**Soluciones:**
1. Verifica que el contenedor de PostgreSQL esté corriendo:
   ```bash
   docker compose -f src/main/resources/compose/docker-compose.yml ps
   ```

2. Verifica que `POSTGRES_HOST` y `POSTGRES_PORT` sean correctos

3. Verifica que las credenciales en `.env` coincidan con las de docker-compose

### Error: Variables no se cargan

**Problema:** Las variables del .env no se están leyendo

**Soluciones:**
1. Verifica que el archivo `.env` existe en la raíz del proyecto
2. Verifica que no tenga errores de sintaxis (sin espacios alrededor del `=`)
3. Reinicia la aplicación o Docker Compose después de cambiar `.env`

### Error: Authentication failed

**Problema:** Error de autenticación con PostgreSQL

**Soluciones:**
1. Verifica que `POSTGRES_USER` y `POSTGRES_PASSWORD` sean correctos
2. Si cambias las credenciales, elimina el volumen de Docker:
   ```bash
   docker compose -f src/main/resources/compose/docker-compose.yml down -v
   docker compose -f src/main/resources/compose/docker-compose.yml up -d
   ```

## Ejemplos de Configuración

### Desarrollo Local Simple
```properties
POSTGRES_USER=dev
POSTGRES_PASSWORD=dev123
POSTGRES_DB=myapp_dev
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
```

### Desarrollo con Docker Network
```properties
POSTGRES_USER=dev
POSTGRES_PASSWORD=dev123
POSTGRES_DB=myapp_dev
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
```

### Staging
```properties
POSTGRES_USER=staging_user
POSTGRES_PASSWORD=StAgInG_P@ssw0rd_2024
POSTGRES_DB=myapp_staging
POSTGRES_HOST=db-staging.internal
POSTGRES_PORT=5432
```

### Producción
```properties
POSTGRES_USER=prod_user
POSTGRES_PASSWORD=Pr0d_S3cur3_P@ssw0rd_2024!
POSTGRES_DB=myapp_production
POSTGRES_HOST=db-prod.example.com
POSTGRES_PORT=5432
```

## Referencias

- [Quarkus Configuration Guide](https://quarkus.io/guides/config-reference)
- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [12-Factor App: Config](https://12factor.net/config)
