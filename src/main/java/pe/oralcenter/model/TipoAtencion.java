package pe.oralcenter.model;

public class TipoAtencion {
    private final long id;
    private final String nombre;
    private final int duracionSugerida;

    public TipoAtencion(long id, String nombre, int duracionSugerida) {
        this.id = id;
        this.nombre = nombre;
        this.duracionSugerida = duracionSugerida;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getDuracionSugerida() { return duracionSugerida; }
    @Override public String toString() { return id + " · " + nombre; }
}
