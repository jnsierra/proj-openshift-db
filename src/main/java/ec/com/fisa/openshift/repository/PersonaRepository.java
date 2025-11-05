package ec.com.fisa.openshift.repository;

import ec.com.fisa.openshift.entity.Persona;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonaRepository implements PanacheRepository<Persona> {

}
