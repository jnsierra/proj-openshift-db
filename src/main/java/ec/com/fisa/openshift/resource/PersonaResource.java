package ec.com.fisa.openshift.resource;

import ec.com.fisa.openshift.entity.Persona;
import ec.com.fisa.openshift.repository.PersonaRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/personas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonaResource {

    @Inject
    PersonaRepository personaRepository;

    @GET
    public List<Persona> listarTodas() {
        return personaRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") Long id) {
        Persona persona = personaRepository.findById(id);
        if (persona == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(persona).build();
    }

    @POST
    @Transactional
    public Response crear(Persona persona) {
        personaRepository.persist(persona);
        return Response.status(Response.Status.CREATED).entity(persona).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response actualizar(@PathParam("id") Long id, Persona personaActualizada) {
        Persona persona = personaRepository.findById(id);
        if (persona == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        persona.nombre = personaActualizada.nombre;
        persona.apellido = personaActualizada.apellido;
        persona.edad = personaActualizada.edad;
        personaRepository.persist(persona);
        return Response.ok(persona).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response eliminar(@PathParam("id") Long id) {
        boolean eliminado = personaRepository.deleteById(id);
        if (!eliminado) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}
