package ec.com.fisa.openshift.infrastructure.rest;

import ec.com.fisa.openshift.application.service.PersonaService;
import ec.com.fisa.openshift.domain.model.Persona;
import ec.com.fisa.openshift.infrastructure.rest.dto.PersonaDTO;
import ec.com.fisa.openshift.infrastructure.rest.mapper.PersonaDTOMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/api/personas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonaResource {

    @Inject
    PersonaService personaService;

    @Inject
    PersonaDTOMapper mapper;

    @GET
    public Response listarTodas() {
        List<PersonaDTO> personas = personaService.listarTodas().stream()
            .map(mapper::toDTO)
            .toList();
        return Response.ok(personas).build();
    }

    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") Long id) {
        Optional<Persona> persona = personaService.obtenerPorId(id);
        if (persona.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.toDTO(persona.get())).build();
    }

    @POST
    public Response crear(PersonaDTO personaDTO) {
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
    public Response actualizar(@PathParam("id") Long id, PersonaDTO personaDTO) {
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
    public Response eliminar(@PathParam("id") Long id) {
        boolean eliminado = personaService.eliminar(id);
        if (!eliminado) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
