# CI/CD con GitHub Actions

Este proyecto utiliza GitHub Actions para automatizar el build y despliegue de imágenes Docker.

## Pipeline de CI/CD

El pipeline está definido en `.github/workflows/main.yml` y se ejecuta automáticamente cuando:
- Se hace push a la rama `main`
- Se ejecuta manualmente desde GitHub Actions (workflow_dispatch)

## Flujo del Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                      GitHub Actions Workflow                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │ 1. Checkout Code │
                    └──────────────────┘
                              │
                              ▼
                ┌──────────────────────────────┐
                │ 2. Verify Dockerfile Exists  │
                │    (Debug Step)              │
                └──────────────────────────────┘
                              │
                              ▼
                  ┌────────────────────────┐
                  │ 3. Setup Docker Buildx │
                  └────────────────────────┘
                              │
                              ▼
                    ┌──────────────────────┐
                    │ 4. Login to Docker Hub│
                    └──────────────────────┘
                              │
                              ▼
                 ┌──────────────────────────────┐
                 │ 5. Extract Metadata (tags)   │
                 └──────────────────────────────┘
                              │
                              ▼
              ┌─────────────────────────────────────┐
              │ 6. Build & Push Docker Image        │
              │    - Build with cache               │
              │    - Push to Docker Hub             │
              └─────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │ 7. Build Summary │
                    └──────────────────┘
```

## Pasos del Pipeline

### 1. Checkout Repository
Clona el código del repositorio en el runner de GitHub Actions.

```yaml
- name: Checkout repository
  uses: actions/checkout@v4
```

### 2. Verify Dockerfile Exists (Debug)
Paso de debug que verifica que el Dockerfile existe y muestra su contenido.

```yaml
- name: Verify Dockerfile exists
  run: |
    ls -la
    if [ -f "Dockerfile" ]; then
      echo "✓ Dockerfile found"
      cat Dockerfile | head -20
    else
      echo "✗ Dockerfile NOT found"
      exit 1
    fi
```

**Propósito:** Prevenir errores de "Dockerfile not found" identificando el problema temprano.

### 3. Set up Docker Buildx
Configura Docker Buildx para builds avanzados y eficientes.

```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3
```

**Beneficios:**
- Builds multi-plataforma
- Cache eficiente
- Build paralelo

### 4. Login to Docker Hub
Autentica contra Docker Hub usando secrets.

```yaml
- name: Log in to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKER_USERNAME }}
    password: ${{ secrets.DOCKERHUB_TOKEN }}
```

### 5. Extract Metadata
Genera tags y labels para la imagen Docker.

```yaml
- name: Extract metadata for Docker
  id: meta
  uses: docker/metadata-action@v5
  with:
    images: ${{ secrets.DOCKER_USERNAME }}/proj-openshift-db
    tags: |
      type=raw,value=latest
      type=raw,value=build-${{ github.run_number }}
      type=sha,prefix={{branch}}-
```

**Tags generados:**
- `latest` - Última versión estable
- `build-{número}` - Build específico (ej: build-42)
- `{branch}-{sha}` - Tag con el commit SHA (ej: main-abc1234)

### 6. Build and Push Docker Image
Construye la imagen Docker y la sube a Docker Hub.

```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    context: .
    file: ./Dockerfile
    push: true
    tags: ${{ steps.meta.outputs.tags }}
    labels: ${{ steps.meta.outputs.labels }}
    cache-from: type=gha
    cache-to: type=gha,mode=max
```

**Características:**
- `context: .` - Usa el directorio raíz como contexto
- `file: ./Dockerfile` - Especifica explícitamente el Dockerfile
- `cache-from/cache-to: type=gha` - Usa cache de GitHub Actions para acelerar builds

### 7. Build Summary
Genera un resumen visible en la interfaz de GitHub Actions.

```yaml
- name: Build summary
  run: |
    echo "## Docker Build Successful! 🚀" >> $GITHUB_STEP_SUMMARY
    echo "**Image:** \`${{ secrets.DOCKER_USERNAME }}/proj-openshift-db\`" >> $GITHUB_STEP_SUMMARY
    echo "**Tags:**" >> $GITHUB_STEP_SUMMARY
    echo "${{ steps.meta.outputs.tags }}" | sed 's/^/- /' >> $GITHUB_STEP_SUMMARY
```

## Configuración de Secrets

Para que el pipeline funcione, debes configurar los siguientes secrets en GitHub:

### Paso 1: Crear Token de Docker Hub

1. Inicia sesión en [Docker Hub](https://hub.docker.com)
2. Ve a **Account Settings → Security → New Access Token**
3. Nombra el token: `github-actions`
4. Copia el token generado

### Paso 2: Configurar Secrets en GitHub

1. Ve a tu repositorio en GitHub
2. Click en **Settings → Secrets and variables → Actions**
3. Click en **New repository secret**
4. Añade estos dos secrets:

| Secret Name | Value | Descripción |
|-------------|-------|-------------|
| `DOCKER_USERNAME` | tu-usuario-dockerhub | Tu nombre de usuario de Docker Hub |
| `DOCKERHUB_TOKEN` | token-generado | El token que creaste en Docker Hub |

### Verificación

```bash
# Los secrets deben estar configurados antes de ejecutar el workflow
# No se pueden ver los valores, solo los nombres
```

## Triggers del Pipeline

### Push a Main
El pipeline se ejecuta automáticamente en cada push a `main`:

```bash
git push origin main
```

### Ejecución Manual
Desde la interfaz de GitHub:

1. Ve a **Actions**
2. Selecciona el workflow **CI-Proj-openshift-db**
3. Click en **Run workflow**
4. Selecciona la rama
5. Click en **Run workflow**

## Cache de Capas Docker

El pipeline utiliza cache de GitHub Actions para acelerar los builds:

```yaml
cache-from: type=gha
cache-to: type=gha,mode=max
```

**Beneficios:**
- Primer build: ~5-10 minutos
- Builds posteriores (con cache): ~2-3 minutos
- Ahorro de ~70% en tiempo de build

**Modo `max`:** Cachea todas las capas intermedias, no solo las capas finales.

## Monitoreo del Pipeline

### Ver el Estado del Workflow

1. Ve a la pestaña **Actions** en GitHub
2. Selecciona el workflow en ejecución
3. Expande cada paso para ver los logs

### Logs de Build

Los logs muestran:
- Salida del build de Maven
- Capas de Docker siendo creadas
- Push a Docker Hub
- Tags generados

### Verificar la Imagen

Después del build exitoso:

```bash
# Pull desde Docker Hub
docker pull tu-usuario/proj-openshift-db:latest

# Ver tags disponibles
# Visita: https://hub.docker.com/r/tu-usuario/proj-openshift-db/tags
```

## Troubleshooting

### Error: Dockerfile not found

**Causa:** El Dockerfile no existe o no está en la raíz del repositorio.

**Solución:**
1. Verifica que el Dockerfile está en la raíz:
   ```bash
   git ls-files | grep Dockerfile
   ```

2. Si no está, agrégalo:
   ```bash
   git add Dockerfile
   git commit -m "Add Dockerfile"
   git push
   ```

3. El paso de debug "Verify Dockerfile exists" identificará este problema.

### Error: Invalid credentials

**Causa:** Secrets mal configurados o token inválido.

**Solución:**
1. Verifica que los secrets existen en GitHub
2. Regenera el token en Docker Hub
3. Actualiza el secret `DOCKERHUB_TOKEN`

### Error: Build timeout

**Causa:** Build muy lento, posiblemente sin cache.

**Solución:**
1. El cache debería funcionar automáticamente
2. Verifica que `cache-from` y `cache-to` estén configurados
3. En caso extremo, aumenta el timeout:
   ```yaml
   jobs:
     build:
       timeout-minutes: 30  # Default: 360
   ```

### Error: Push access denied

**Causa:** No tienes permisos para push al repositorio de Docker Hub.

**Solución:**
1. Verifica que el repositorio existe en Docker Hub
2. Crea el repositorio si no existe:
   - Visita Docker Hub
   - Click en "Create Repository"
   - Nombre: `proj-openshift-db`
   - Visibilidad: Public o Private
3. Verifica que el `DOCKER_USERNAME` sea correcto

### Build exitoso pero imagen no actualizada

**Causa:** Estás usando cache y no hay cambios.

**Solución:**
```bash
# Forzar rebuild sin cache (solo en caso necesario)
# Añade esto temporalmente al workflow:
docker/build-push-action@v5
  with:
    no-cache: true  # Añade esta línea
```

## Mejoras Futuras

### Tests Automatizados

Añadir tests antes del build:

```yaml
- name: Run tests
  run: ./mvnw test

- name: Build and push Docker image
  uses: docker/build-push-action@v5
  # ... solo si los tests pasan
```

### Build Multi-Plataforma

Soporte para ARM64:

```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    platforms: linux/amd64,linux/arm64
```

### Análisis de Seguridad

Escaneo de vulnerabilidades:

```yaml
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: ${{ secrets.DOCKER_USERNAME }}/proj-openshift-db:latest
```

### Notificaciones

Notificar en Slack/Discord:

```yaml
- name: Notify on success
  if: success()
  run: |
    curl -X POST ${{ secrets.SLACK_WEBHOOK }} \
      -H 'Content-Type: application/json' \
      -d '{"text":"Build successful!"}'
```

### Deploy Automático

Deploy a staging después del build:

```yaml
- name: Deploy to staging
  if: github.ref == 'refs/heads/main'
  run: |
    # Script de deploy
```

## Badges

Añade badges al README para mostrar el estado del build:

```markdown
[![CI-Proj-openshift-db](https://github.com/tu-usuario/proj-openshift-db/actions/workflows/main.yml/badge.svg)](https://github.com/tu-usuario/proj-openshift-db/actions/workflows/main.yml)
```

## Costos

### GitHub Actions
- Repositorios públicos: **Gratis e ilimitado**
- Repositorios privados: 2000 minutos/mes gratis (plan Free)

### Docker Hub
- Pulls: Ilimitados para repositorios públicos
- Imagen privada: 1 repositorio gratis

## Best Practices

1. **Tags semánticos:** Usa tags con versión semántica (v1.0.0)
2. **Cache:** Siempre habilita cache para builds más rápidos
3. **Secrets:** Nunca expongas credenciales en logs
4. **Matrix builds:** Construye para múltiples plataformas si es necesario
5. **Fail fast:** Falla rápido si hay problemas (ej: tests fallidos)

## Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [Docker Hub](https://hub.docker.com)
- [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
