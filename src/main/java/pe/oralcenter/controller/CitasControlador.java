package pe.oralcenter.controller;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.oralcenter.dao.CitaDAO;
import pe.oralcenter.export.AgendaExcel;
import pe.oralcenter.model.*;

public class CitasControlador {
    private static final Logger REGISTRO = LoggerFactory.getLogger(CitasControlador.class);
    private final CitaDAO citas;
    private final AccesoControlador acceso;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public CitasControlador(CitaDAO citas, AccesoControlador acceso) {
        this.citas = citas;
        this.acceso = acceso;
    }

    public List<Paciente> listarPacientes() { return citas.listarPacientes(odontologoPermitido()); }
    public List<CitaAgenda> consultarAgenda(String fecha, Long odontologo, String estado) {
        Long permitido = odontologoPermitido();
        if (odontologo != null) { comprobarOdontologo(odontologo, permitido); }
        if (permitido != null) { odontologo = permitido; }
        if (estado == null || !List.of("Todos", "PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA").contains(estado)) {
            throw new IllegalArgumentException("Selecciona un estado válido para consultar la agenda.");
        }
        if (estado.equals("Todos")) { estado = ""; }
        return citas.consultarAgenda(leerFecha(fecha), odontologo, estado);
    }
    public List<Odontologo> listarOdontologos() { return citas.listarOdontologos(odontologoPermitido()); }
    public int exportarAgenda(Path archivo, String fecha, Long odontologo, String estado) {
        List<CitaAgenda> lista = consultarAgenda(fecha, odontologo, estado);
        if (lista.isEmpty()) { throw new IllegalArgumentException("No hay citas para exportar con estos filtros."); }
        String profesional = "Todos los autorizados";
        if (odontologo != null) { profesional = lista.get(0).getOdontologo(); }
        try {
            new AgendaExcel().guardar(archivo, lista, leerFecha(fecha).format(FECHA), profesional, estado);
            return lista.size();
        } catch (FileAlreadyExistsException ex) {
            throw new IllegalStateException("Ya existe un archivo con ese nombre. Elige otro nombre para el Excel.");
        } catch (IOException ex) {
            REGISTRO.error("Falló la exportación de la agenda.");
            throw new IllegalStateException("No se pudo guardar el Excel. Revisa la carpeta y sus permisos e inténtalo nuevamente.");
        }
    }
    public List<TipoAtencion> listarTiposAtencion() {
        odontologoPermitido();
        return citas.listarTiposAtencion();
    }

    public List<Intervalo> consultarDisponibilidad(long odontologo, String fecha, String duracion) {
        comprobarOdontologo(odontologo, odontologoPermitido());
        return citas.consultarDisponibilidad(odontologo, leerFecha(fecha), leerDuracion(duracion));
    }

    public long registrar(long paciente, long odontologo, long tipo, String fecha, String hora, String duracion, String motivo) {
        Long permitido = odontologoPermitido();
        comprobarOdontologo(odontologo, permitido);
        if (paciente <= 0 || tipo <= 0) {
            throw new IllegalArgumentException("Selecciona un paciente y un tipo de atención válidos.");
        }
        if (StringUtils.isBlank(motivo)) {
            throw new IllegalArgumentException("Completa el motivo de consulta.");
        }
        String texto = StringUtils.trim(motivo);
        if (texto.length() > 300) {
            throw new IllegalArgumentException("El motivo permite hasta 300 caracteres.");
        }
        Cita cita = new Cita(0, paciente, odontologo, tipo, leerFecha(fecha), leerHora(hora), leerDuracion(duracion), texto, "PENDIENTE");
        if (!cita.getInicio().toLocalDate().equals(cita.getFin().toLocalDate())) {
            throw new IllegalArgumentException("La cita completa debe quedar dentro del mismo día.");
        }
        return citas.guardar(cita, permitido);
    }

    private Long odontologoPermitido() {
        Usuario usuario = acceso.getUsuarioActual();
        if (usuario.getPerfiles().contains(Perfil.ADMINISTRADOR) || usuario.getPerfiles().contains(Perfil.RECEPCIONISTA)) {
            return null;
        }
        if (usuario.getPerfiles().contains(Perfil.ODONTOLOGO) && usuario.getIdOdontologo() != null) {
            return usuario.getIdOdontologo();
        }
        throw new IllegalStateException("Tu perfil no permite gestionar citas.");
    }

    private void comprobarOdontologo(long id, Long permitido) {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un odontólogo válido.");
        }
        if (permitido != null && permitido.longValue() != id) {
            throw new IllegalStateException("Solo puedes gestionar citas de tu propio odontólogo.");
        }
    }

    private LocalDate leerFecha(String texto) {
        if (texto != null) {
            try {
                LocalDate fecha = LocalDate.parse(texto.trim(), FECHA);
                if (fecha.getYear() >= 1000 && fecha.getYear() <= 9999) {
                    return fecha;
                }
            } catch (DateTimeParseException ex) { }
        }
        throw new IllegalArgumentException("Escribe una fecha válida en formato día/mes/año, por ejemplo 02/11/2026.");
    }

    private LocalTime leerHora(String texto) {
        if (texto != null) {
            try {
                LocalTime hora = LocalTime.parse(texto.trim());
                if (hora.getNano() == 0) {
                    return hora;
                }
            } catch (DateTimeParseException ex) { }
        }
        throw new IllegalArgumentException("Escribe una hora válida de 24 horas, por ejemplo 09:00.");
    }

    private int leerDuracion(String texto) {
        if (texto != null) {
            try {
                int minutos = Integer.parseInt(texto.trim());
                if (minutos > 0 && minutos < 1440) {
                    return minutos;
                }
            } catch (NumberFormatException ex) { }
        }
        throw new IllegalArgumentException("La duración debe ser un número entero de 1 a 1439 minutos y caber en el día.");
    }
}
