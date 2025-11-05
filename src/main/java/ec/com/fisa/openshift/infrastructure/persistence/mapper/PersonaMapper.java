package ec.com.fisa.openshift.infrastructure.persistence.mapper;

import ec.com.fisa.openshift.domain.model.Persona;
import ec.com.fisa.openshift.infrastructure.persistence.entity.PersonaEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonaMapper {

    public Persona toDomain(PersonaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Persona(
            entity.id,
            entity.nombre,
            entity.apellido,
            entity.edad
        );
    }

    public PersonaEntity toEntity(Persona persona) {
        if (persona == null) {
            return null;
        }
        PersonaEntity entity = new PersonaEntity(
            persona.getNombre(),
            persona.getApellido(),
            persona.getEdad()
        );
        if (persona.getId() != null) {
            entity.id = persona.getId();
        }
        return entity;
    }

    public void updateEntity(Persona persona, PersonaEntity entity) {
        entity.nombre = persona.getNombre();
        entity.apellido = persona.getApellido();
        entity.edad = persona.getEdad();
    }
}
