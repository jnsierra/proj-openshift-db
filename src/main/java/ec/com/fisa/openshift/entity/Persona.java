package ec.com.fisa.openshift.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "persona")
public class Persona extends PanacheEntity {

    @Column(nullable = false, length = 100)
    public String nombre;

    @Column(nullable = false, length = 100)
    public String apellido;

    @Column(nullable = false)
    public Integer edad;

    public Persona() {
    }

    public Persona(String nombre, String apellido, Integer edad) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
    }
}
