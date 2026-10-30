package pe.oralcenter.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Cita {
    private final long id;
    private final long idPaciente;
    private final long idOdontologo;
    private final long idTipoAtencion;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final int duracion;
    private final String motivo;
    private final String estado;

    public Cita(long id, long paciente, long odontologo, long tipo, LocalDate fecha,
            LocalTime hora, int duracion, String motivo, String estado) {
        this.id = id;
        this.idPaciente = paciente;
        this.idOdontologo = odontologo;
        this.idTipoAtencion = tipo;
        this.fecha = fecha;
        this.hora = hora;
        this.duracion = duracion;
        this.motivo = motivo;
        this.estado = estado;
    }

    public long getId() { return id; }
    public long getIdPaciente() { return idPaciente; }
    public long getIdOdontologo() { return idOdontologo; }
    public long getIdTipoAtencion() { return idTipoAtencion; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public int getDuracion() { return duracion; }
    public String getMotivo() { return motivo; }
    public String getEstado() { return estado; }
    public LocalDateTime getInicio() { return LocalDateTime.of(fecha, hora); }
    public LocalDateTime getFin() { return getInicio().plusMinutes(duracion); }
    public boolean estaActiva() { return estado.equals("PENDIENTE") || estado.equals("CONFIRMADA"); }
}
