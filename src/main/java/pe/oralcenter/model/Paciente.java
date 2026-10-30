package pe.oralcenter.model;

/** Datos básicos que se guardan en la tabla paciente. */
public class Paciente {
    private final long id;
    private final String dni;
    private final String nombres;
    private final String apellidos;
    private final String telefono;

    public Paciente(long id, String dni, String nombres, String apellidos, String telefono) {
        this.id = id;
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    public long getId() { return id; }
    public String getDni() { return dni; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    @Override public String toString() { return id + " · " + nombres + " " + apellidos + " · " + dni; }
}
