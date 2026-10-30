package pe.oralcenter.model;

/** Una cita con los nombres que se muestran en la agenda. */
public class CitaAgenda {
    private final Cita cita;
    private final String paciente;
    private final String odontologo;
    private final String atencion;

    public CitaAgenda(Cita cita, String paciente, String odontologo, String atencion) {
        this.cita = cita;
        this.paciente = paciente;
        this.odontologo = odontologo;
        this.atencion = atencion;
    }

    public Cita getCita() { return cita; }
    public String getPaciente() { return paciente; }
    public String getOdontologo() { return odontologo; }
    public String getAtencion() { return atencion; }
}
