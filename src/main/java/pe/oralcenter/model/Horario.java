package pe.oralcenter.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Horario {
    private final long id;
    private final long idOdontologo;
    private final int diaSemana;
    private final LocalTime inicio;
    private final LocalTime fin;

    public Horario(long id, long idOdontologo, int diaSemana, LocalTime inicio, LocalTime fin) {
        this.id = id;
        this.idOdontologo = idOdontologo;
        this.diaSemana = diaSemana;
        this.inicio = inicio;
        this.fin = fin;
    }

    public long getId() { return id; }
    public long getIdOdontologo() { return idOdontologo; }
    public int getDiaSemana() { return diaSemana; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFin() { return fin; }

    public boolean seCruzaCon(Horario otro) {
        return idOdontologo == otro.idOdontologo && diaSemana == otro.diaSemana
                && inicio.isBefore(otro.fin) && fin.isAfter(otro.inicio);
    }

    public boolean cubre(LocalDateTime inicioCita, LocalDateTime finCita) {
        return inicioCita.isBefore(finCita)
                && inicioCita.toLocalDate().equals(finCita.toLocalDate())
                && inicioCita.getDayOfWeek().getValue() == diaSemana
                && !inicioCita.toLocalTime().isBefore(inicio)
                && !finCita.toLocalTime().isAfter(fin);
    }
}
