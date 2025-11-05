package ec.com.fisa.openshift.application.service;

import ec.com.fisa.openshift.domain.model.Persona;
import ec.com.fisa.openshift.domain.port.PersonaPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PersonaService {

    @Inject
    PersonaPort personaPort;

    public List<Persona> listarTodas() {
        return personaPort.findAll();
    }

    public Optional<Persona> obtenerPorId(Long id) {
        return personaPort.findById(id);
    }

    public Persona crear(Persona persona) {
        // Aquí se puede agregar validaciones de negocio
        if (persona.getNombre() == null || persona.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (persona.getApellido() == null || persona.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es requerido");
        }
        if (persona.getEdad() == null || persona.getEdad() < 0) {
            throw new IllegalArgumentException("La edad debe ser un valor positivo");
        }
        return personaPort.save(persona);
    }

    public Optional<Persona> actualizar(Long id, Persona personaActualizada) {
        Optional<Persona> personaExistente = personaPort.findById(id);
        if (personaExistente.isEmpty()) {
            return Optional.empty();
        }

        // Validaciones de negocio
        if (personaActualizada.getNombre() == null || personaActualizada.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (personaActualizada.getApellido() == null || personaActualizada.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es requerido");
        }
        if (personaActualizada.getEdad() == null || personaActualizada.getEdad() < 0) {
            throw new IllegalArgumentException("La edad debe ser un valor positivo");
        }

        Persona persona = personaExistente.get();
        persona.setNombre(personaActualizada.getNombre());
        persona.setApellido(personaActualizada.getApellido());
        persona.setEdad(personaActualizada.getEdad());

        return Optional.of(personaPort.save(persona));
    }

    public boolean eliminar(Long id) {
        return personaPort.deleteById(id);
    }
}
