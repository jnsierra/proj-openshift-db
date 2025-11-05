package ec.com.fisa.openshift.infrastructure.persistence.adapter;

import ec.com.fisa.openshift.domain.model.Persona;
import ec.com.fisa.openshift.domain.port.PersonaPort;
import ec.com.fisa.openshift.infrastructure.persistence.entity.PersonaEntity;
import ec.com.fisa.openshift.infrastructure.persistence.mapper.PersonaMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class PersonaRepositoryAdapter implements PersonaPort {

    @Inject
    PersonaMapper mapper;

    @Override
    public List<Persona> findAll() {
        return PersonaEntity.listAll().stream()
            .map(entity -> mapper.toDomain((PersonaEntity) entity))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Persona> findById(Long id) {
        PersonaEntity entity = PersonaEntity.findById(id);
        return Optional.ofNullable(mapper.toDomain(entity));
    }

    @Override
    @Transactional
    public Persona save(Persona persona) {
        PersonaEntity entity;
        if (persona.getId() != null) {
            entity = PersonaEntity.findById(persona.getId());
            if (entity != null) {
                mapper.updateEntity(persona, entity);
                entity.persist();
            } else {
                entity = mapper.toEntity(persona);
                entity.persist();
            }
        } else {
            entity = mapper.toEntity(persona);
            entity.persist();
        }
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        return PersonaEntity.deleteById(id);
    }
}
