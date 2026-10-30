package pe.oralcenter.controller;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import pe.oralcenter.dao.OdontologoDAO;
import pe.oralcenter.model.Horario;
import pe.oralcenter.model.Odontologo;
import pe.oralcenter.model.Perfil;

public class OdontologosControlador {
    private final OdontologoDAO odontologos;
    private final AccesoControlador acceso;

    public OdontologosControlador(OdontologoDAO odontologos, AccesoControlador acceso) {
        this.odontologos = odontologos;
        this.acceso = acceso;
    }

    public void registrar(String dni, String nombre, String especialidad) {
        acceso.exigirPerfil(Perfil.ADMINISTRADOR);
        odontologos.guardar(crearOdontologo(0, dni, nombre, especialidad));
    }

    public void actualizar(long id, String dni, String nombre, String especialidad) {
        acceso.exigirPerfil(Perfil.ADMINISTRADOR);
        validarCodigo(id);
        odontologos.actualizar(crearOdontologo(id, dni, nombre, especialidad));
    }

    public List<Odontologo> listar() {
        acceso.exigirPerfil(Perfil.ADMINISTRADOR);
        return odontologos.listar();
    }

    public void registrarHorario(long odontologo, int dia, String inicio, String fin) {
        acceso.exigirPerfil(Perfil.ADMINISTRADOR);
        odontologos.guardarHorario(crearHorario(0, odontologo, dia, inicio, fin));
    }

    public void actualizarHorario(long id, long odontologo, int dia, String inicio, String fin) {
        acceso.exigirPerfil(Perfil.ADMINISTRADOR);
        validarCodigo(id);
        odontologos.guardarHorario(crearHorario(id, odontologo, dia, inicio, fin));
    }

    public List<Horario> listarHorarios(long odontologo) {
        acceso.exigirPerfil(Perfil.ADMINISTRADOR);
        validarCodigo(odontologo);
        return odontologos.listarHorarios(odontologo);
    }

    private Odontologo crearOdontologo(long id, String dni, String nombre, String especialidad) {
        return new Odontologo(id, validarTexto(dni, "DNI", 20),
                validarTexto(nombre, "nombre completo", 200), validarTexto(especialidad, "especialidad", 100));
    }

    private Horario crearHorario(long id, long odontologo, int dia, String inicio, String fin) {
        validarCodigo(odontologo);
        if (dia < 1 || dia > 7) {
            throw new IllegalArgumentException("Selecciona un día de la semana válido.");
        }
        LocalTime desde = leerHora(inicio);
        LocalTime hasta = leerHora(fin);
        if (!desde.isBefore(hasta)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin del mismo día.");
        }
        return new Horario(id, odontologo, dia, desde, hasta);
    }

    private LocalTime leerHora(String texto) {
        if (texto != null) {
            try {
                LocalTime hora = LocalTime.parse(texto.trim());
                if (hora.getNano() == 0) {
                    return hora;
                }
            } catch (DateTimeParseException ex) {
                // El mensaje siguiente indica el formato que debe ingresar el usuario.
            }
        }
        throw new IllegalArgumentException("Escribe una hora válida de 24 horas, por ejemplo 09:00 o 14:30.");
    }

    private void validarCodigo(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un registro válido de la tabla.");
        }
    }

    private String validarTexto(String valor, String campo, int limite) {
        if (StringUtils.isBlank(valor)) {
            throw new IllegalArgumentException("Completa el campo " + campo + ".");
        }
        String texto = StringUtils.trim(valor);
        if (texto.length() > limite) {
            throw new IllegalArgumentException("El campo " + campo + " permite hasta " + limite + " caracteres.");
        }
        return texto;
    }
}
