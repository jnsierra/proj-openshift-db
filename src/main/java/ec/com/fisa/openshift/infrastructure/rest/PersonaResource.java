package ec.com.fisa.openshift.infrastructure.rest;

import ec.com.fisa.openshift.application.service.PersonaService;
import ec.com.fisa.openshift.domain.model.Persona;
import ec.com.fisa.openshift.infrastructure.rest.dto.PersonaDTO;
import ec.com.fisa.openshift.infrastructure.rest.mapper.PersonaDTOMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/api/personas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Personas", description = "Operaciones para gestionar personas")
public class PersonaResource {

    @Inject
    PersonaService personaService;

    @Inject
    PersonaDTOMapper mapper;

    @GET
    @Operation(summary = "Listar todas las personas", description = "Obtiene un listado completo de todas las personas registradas en el sistema")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Listado obtenido exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PersonaDTO.class)))
    })
    public Response listarTodas() {
        List<PersonaDTO> personas = personaService.listarTodas().stream()
            .map(mapper::toDTO)
            .toList();
        return Response.ok(personas).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener persona por ID", description = "Busca y retorna una persona específica por su identificador único")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Persona encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PersonaDTO.class))),
        @APIResponse(responseCode = "404", description = "Persona no encontrada")
    })
    public Response obtenerPorId(
        @Parameter(description = "ID de la persona a buscar", required = true, example = "1")
        @PathParam("id") Long id) {
        Optional<Persona> persona = personaService.obtenerPorId(id);
        if (persona.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.toDTO(persona.get())).build();
    }

    @POST
    @Operation(summary = "Crear nueva persona", description = "Registra una nueva persona en el sistema con nombre, apellido y edad")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Persona creada exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PersonaDTO.class))),
        @APIResponse(responseCode = "400", description = "Datos inválidos o incompletos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response crear(
        @Parameter(description = "Datos de la persona a crear", required = true)
        PersonaDTO personaDTO) {
        try {
            Persona persona = mapper.toDomain(personaDTO);
            Persona personaCreada = personaService.crear(persona);
            return Response.status(Response.Status.CREATED)
                .entity(mapper.toDTO(personaCreada))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar persona", description = "Actualiza los datos de una persona existente")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Persona actualizada exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PersonaDTO.class))),
        @APIResponse(responseCode = "400", description = "Datos inválidos o incompletos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Persona no encontrada")
    })
    public Response actualizar(
        @Parameter(description = "ID de la persona a actualizar", required = true, example = "1")
        @PathParam("id") Long id,
        @Parameter(description = "Nuevos datos de la persona", required = true)
        PersonaDTO personaDTO) {
        try {
            Persona persona = mapper.toDomain(personaDTO);
            Optional<Persona> personaActualizada = personaService.actualizar(id, persona);
            if (personaActualizada.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(mapper.toDTO(personaActualizada.get())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar persona", description = "Elimina una persona del sistema de forma permanente")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Persona eliminada exitosamente"),
        @APIResponse(responseCode = "404", description = "Persona no encontrada")
    })
    public Response eliminar(
        @Parameter(description = "ID de la persona a eliminar", required = true, example = "1")
        @PathParam("id") Long id) {
        boolean eliminado = personaService.eliminar(id);
        if (!eliminado) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    @Schema(description = "Respuesta de error para solicitudes inválidas")
    public static class ErrorResponse {
        @Schema(description = "Mensaje descriptivo del error", example = "El nombre es requerido")
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
