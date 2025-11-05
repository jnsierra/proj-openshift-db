package ec.com.fisa.openshift.infrastructure.rest.mapper;

import ec.com.fisa.openshift.domain.model.Persona;
import ec.com.fisa.openshift.infrastructure.rest.dto.PersonaDTO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonaDTOMapper {

    public PersonaDTO toDTO(Persona persona) {
        if (persona == null) {
            return null;
        }
        return new PersonaDTO(
            persona.getId(),
            persona.getNombre(),
            persona.getApellido(),
            persona.getEdad()
        );
    }

    public Persona toDomain(PersonaDTO dto) {
        if (dto == null) {
            return null;
        }
        return new Persona(
            dto.getId(),
            dto.getNombre(),
            dto.getApellido(),
            dto.getEdad()
        );
    }
}
