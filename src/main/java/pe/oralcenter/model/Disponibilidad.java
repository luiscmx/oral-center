package pe.oralcenter.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Disponibilidad {
    public List<Intervalo> calcular(long odontologo, LocalDate fecha, int duracion,
            List<Horario> horarios, List<Cita> citas, LocalDateTime ahora) {
        if (duracion <= 0 || duracion >= 1440) {
            throw new IllegalArgumentException("La duración debe ser positiva y caber en un mismo día.");
        }
        List<Intervalo> resultado = new ArrayList<>();
        LocalDateTime minimo = ahora.withNano(0);
        if (ahora.getNano() > 0) {
            minimo = minimo.plusSeconds(1);
        }
        for (Horario horario : horarios) {
            if (horario.getIdOdontologo() != odontologo || horario.getDiaSemana() != fecha.getDayOfWeek().getValue()) {
                continue;
            }
            LocalDateTime inicio = fecha.atTime(horario.getInicio());
            LocalDateTime fin = fecha.atTime(horario.getFin());
            if (inicio.isBefore(minimo)) {
                inicio = minimo;
            }
            List<Intervalo> libres = new ArrayList<>();
            if (inicio.isBefore(fin)) {
                libres.add(new Intervalo(inicio, fin));
            }
            for (Cita cita : citas) {
                if (cita.estaActiva() && cita.getIdOdontologo() == odontologo && cita.getFecha().equals(fecha)) {
                    libres = descontarCita(libres, cita);
                }
            }
            for (Intervalo libre : libres) {
                if (!libre.getInicio().plusMinutes(duracion).isAfter(libre.getFin())) {
                    resultado.add(libre);
                }
            }
        }
        ordenarPorHora(resultado);
        return resultado;
    }

    private List<Intervalo> descontarCita(List<Intervalo> libres, Cita cita) {
        List<Intervalo> restantes = new ArrayList<>();
        for (Intervalo libre : libres) {
            boolean hayCruce = cita.getInicio().isBefore(libre.getFin()) && cita.getFin().isAfter(libre.getInicio());
            if (!hayCruce) {
                restantes.add(libre);
            } else {
                // Una cita puede dejar espacio libre antes, después o en ambos lados.
                if (libre.getInicio().isBefore(cita.getInicio())) {
                    restantes.add(new Intervalo(libre.getInicio(), cita.getInicio()));
                }
                if (cita.getFin().isBefore(libre.getFin())) {
                    restantes.add(new Intervalo(cita.getFin(), libre.getFin()));
                }
            }
        }
        return restantes;
    }

    private void ordenarPorHora(List<Intervalo> intervalos) {
        for (int i = 0; i < intervalos.size() - 1; i++) {
            for (int j = i + 1; j < intervalos.size(); j++) {
                if (intervalos.get(j).getInicio().isBefore(intervalos.get(i).getInicio())) {
                    Intervalo temporal = intervalos.get(i);
                    intervalos.set(i, intervalos.get(j));
                    intervalos.set(j, temporal);
                }
            }
        }
    }
}
