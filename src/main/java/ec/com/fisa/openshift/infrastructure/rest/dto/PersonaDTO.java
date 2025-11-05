package ec.com.fisa.openshift.infrastructure.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Objeto de transferencia de datos para Persona")
public class PersonaDTO {

    @Schema(description = "Identificador único de la persona", example = "1", readOnly = true)
    private Long id;

    @Schema(description = "Nombre de la persona", example = "Juan", required = true, minLength = 1, maxLength = 100)
    private String nombre;

    @Schema(description = "Apellido de la persona", example = "Pérez", required = true, minLength = 1, maxLength = 100)
    private String apellido;

    @Schema(description = "Edad de la persona en años", example = "30", required = true, minimum = "0")
    private Integer edad;

    public PersonaDTO() {
    }

    public PersonaDTO(Long id, String nombre, String apellido, Integer edad) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }
}
