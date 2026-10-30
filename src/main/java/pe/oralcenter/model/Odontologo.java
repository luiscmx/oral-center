package pe.oralcenter.model;

public class Odontologo {
    private final long id;
    private final String dni;
    private final String nombreCompleto;
    private final String especialidad;

    public Odontologo(long id, String dni, String nombreCompleto, String especialidad) {
        this.id = id;
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
    }

    public long getId() { return id; }
    public String getDni() { return dni; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getEspecialidad() { return especialidad; }
    @Override public String toString() { return id + " · " + nombreCompleto; }
}
