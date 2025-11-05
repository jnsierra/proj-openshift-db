# Arquitectura Hexagonal (Ports and Adapters)

Este proyecto implementa una arquitectura hexagonal (también conocida como "Ports and Adapters") para mantener la lógica de negocio independiente de los detalles de implementación.

## Estructura del Proyecto

```
ec.com.fisa.openshift/
├── domain/                          # Núcleo del negocio
│   ├── model/                       # Entidades de dominio
│   │   └── Persona.java            # Modelo de dominio Persona
│   └── port/                        # Interfaces (puertos)
│       └── PersonaPort.java        # Puerto para operaciones de Persona
│
├── application/                     # Casos de uso
│   └── service/
│       └── PersonaService.java     # Lógica de negocio y validaciones
│
└── infrastructure/                  # Adaptadores (implementaciones)
    ├── persistence/                # Adaptador de persistencia
    │   ├── entity/
    │   │   └── PersonaEntity.java  # Entidad JPA
    │   ├── mapper/
    │   │   └── PersonaMapper.java  # Mapeo entre Domain y Entity
    │   └── adapter/
    │       └── PersonaRepositoryAdapter.java  # Implementación del puerto
    │
    └── rest/                       # Adaptador REST
        ├── dto/
        │   └── PersonaDTO.java     # DTO para API REST
        ├── mapper/
        │   └── PersonaDTOMapper.java  # Mapeo entre Domain y DTO
        └── PersonaResource.java    # Controlador REST
```

## Capas de la Arquitectura

### 1. Domain (Dominio)
**Ubicación:** `ec.com.fisa.openshift.domain`

El núcleo de la aplicación que contiene:
- **Modelos de dominio:** Entidades de negocio puras sin dependencias externas
- **Puertos (Interfaces):** Contratos que definen cómo interactuar con el exterior

**Características:**
- No tiene dependencias de frameworks
- Contiene la lógica de negocio fundamental
- Define interfaces (puertos) que serán implementadas por los adaptadores

### 2. Application (Aplicación)
**Ubicación:** `ec.com.fisa.openshift.application`

Contiene los casos de uso y servicios de aplicación:
- **Servicios:** Orquestan la lógica de negocio
- **Validaciones:** Reglas de negocio
- **Coordinación:** Entre diferentes componentes del dominio

**Características:**
- Depende solo del dominio
- No conoce detalles de implementación (base de datos, REST, etc.)
- Implementa las reglas de negocio y validaciones

### 3. Infrastructure (Infraestructura)
**Ubicación:** `ec.com.fisa.openshift.infrastructure`

Contiene los adaptadores que implementan los detalles técnicos:

#### 3.1 Adaptador de Persistencia
- **PersonaEntity:** Entidad JPA para la base de datos
- **PersonaRepositoryAdapter:** Implementa PersonaPort usando Hibernate/Panache
- **PersonaMapper:** Convierte entre modelos de dominio y entidades JPA

#### 3.2 Adaptador REST
- **PersonaResource:** Controlador REST
- **PersonaDTO:** Objeto de transferencia de datos para la API
- **PersonaDTOMapper:** Convierte entre modelos de dominio y DTOs

## Flujo de Datos

```
Cliente HTTP
    ↓
PersonaResource (REST Adapter)
    ↓
PersonaDTOMapper
    ↓
PersonaService (Application)
    ↓
PersonaPort (Interface)
    ↓
PersonaRepositoryAdapter (Persistence Adapter)
    ↓
PersonaMapper
    ↓
PersonaEntity (JPA)
    ↓
Base de Datos PostgreSQL
```

## Ventajas de esta Arquitectura

1. **Independencia de Frameworks:** El dominio no depende de tecnologías específicas
2. **Testabilidad:** Fácil de probar cada capa de forma independiente
3. **Flexibilidad:** Fácil cambiar implementaciones sin afectar el negocio
4. **Mantenibilidad:** Separación clara de responsabilidades
5. **Reutilización:** La lógica de negocio puede usarse con diferentes adaptadores

## Endpoints REST

- `GET /api/personas` - Listar todas las personas
- `GET /api/personas/{id}` - Obtener persona por ID
- `POST /api/personas` - Crear nueva persona
- `PUT /api/personas/{id}` - Actualizar persona
- `DELETE /api/personas/{id}` - Eliminar persona

## Validaciones de Negocio

El `PersonaService` implementa las siguientes validaciones:
- Nombre no puede ser nulo o vacío
- Apellido no puede ser nulo o vacío
- Edad debe ser un valor positivo

## Ejemplo de Uso

```bash
# Crear una persona
curl -X POST http://localhost:8080/api/personas \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "edad": 30
  }'

# Listar todas las personas
curl http://localhost:8080/api/personas

# Obtener una persona por ID
curl http://localhost:8080/api/personas/1

# Actualizar una persona
curl -X PUT http://localhost:8080/api/personas/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Carlos",
    "apellido": "Pérez López",
    "edad": 31
  }'

# Eliminar una persona
curl -X DELETE http://localhost:8080/api/personas/1
```

## Configuración

El proyecto utiliza variables de entorno para la configuración de la base de datos, siguiendo las mejores prácticas de la metodología 12-Factor App.

### Variables de Entorno

Las credenciales y configuraciones están externalizadas en un archivo `.env`:

```properties
POSTGRES_USER=openshift
POSTGRES_PASSWORD=openshift
POSTGRES_DB=proj-openshift
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
```

Para más información sobre configuración, consulta [CONFIGURACION.md](CONFIGURACION.md).

## Ejecución

1. Configurar el archivo `.env`:
```bash
cp .env.example .env
# Edita el archivo .env con tus credenciales
```

2. Iniciar la base de datos:
```bash
docker compose -f src/main/resources/compose/docker-compose.yml up -d
```

3. Ejecutar la aplicación:
```bash
./mvnw quarkus:dev
```

La aplicación estará disponible en `http://localhost:8080`
