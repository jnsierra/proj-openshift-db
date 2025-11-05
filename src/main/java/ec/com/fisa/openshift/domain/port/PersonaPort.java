package ec.com.fisa.openshift.domain.port;

import ec.com.fisa.openshift.domain.model.Persona;

import java.util.List;
import java.util.Optional;

public interface PersonaPort {
    List<Persona> findAll();
    Optional<Persona> findById(Long id);
    Persona save(Persona persona);
    boolean deleteById(Long id);
}
