package pe.oralcenter.model;

import java.time.LocalDateTime;

/** Un tramo libre de una fecha concreta, no un turno semanal guardado. */
public class Intervalo {
    private final LocalDateTime inicio;
    private final LocalDateTime fin;

    public Intervalo(LocalDateTime inicio, LocalDateTime fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
    public boolean contiene(LocalDateTime desde, LocalDateTime hasta) {
        return desde.isBefore(hasta) && !desde.isBefore(inicio) && !hasta.isAfter(fin);
    }
}
