package pe.oralcenter.controller;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import pe.oralcenter.dao.PacienteDAO;
import pe.oralcenter.model.Paciente;
import pe.oralcenter.model.Perfil;
import pe.oralcenter.model.Usuario;

/** Validaciones y permisos del registro y listado de pacientes. */
public class PacientesControlador {
    private final PacienteDAO pacientes;
    private final AccesoControlador acceso;

    public PacientesControlador(PacienteDAO pacientes, AccesoControlador acceso) {
        this.pacientes = pacientes;
        this.acceso = acceso;
    }

    public void registrar(String dni, String nombres, String apellidos, String telefono) {
        exigirPermiso();
        pacientes.guardar(crearPaciente(0, dni, nombres, apellidos, telefono));
    }

    public List<Paciente> listar() {
        return buscar("");
    }

    public void actualizar(long id, String dni, String nombres, String apellidos, String telefono) {
        exigirPermiso();
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un paciente válido para editar.");
        }
        pacientes.actualizar(crearPaciente(id, dni, nombres, apellidos, telefono));
    }

    public List<Paciente> buscar(String texto) {
        String filtro = StringUtils.trimToEmpty(texto);
        if (puedeGestionar()) { return pacientes.buscar(filtro); }
        Usuario usuario = acceso.getUsuarioActual();
        Long odontologo = usuario.getIdOdontologo();
        if (usuario.getPerfiles().contains(Perfil.ODONTOLOGO) && odontologo != null && odontologo > 0) {
            return pacientes.buscarAsociados(filtro, odontologo);
        }
        throw new IllegalStateException("Tu cuenta no tiene configurada la consulta de pacientes.");
    }

    public void eliminar(long id) {
        exigirPermiso();
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un paciente válido para eliminar.");
        }
        pacientes.eliminar(id);
    }

    private Paciente crearPaciente(long id, String dni, String nombres, String apellidos, String telefono) {
        String documento = validar(dni, "DNI", 20);
        String nombre = validar(nombres, "nombres", 100);
        String apellido = validar(apellidos, "apellidos", 100);
        String contacto = validar(telefono, "teléfono", 30);
        return new Paciente(id, documento, nombre, apellido, contacto);
    }

    private void exigirPermiso() {
        if (!puedeGestionar()) {
            throw new IllegalStateException("Tu perfil no permite gestionar el listado general de pacientes.");
        }
    }

    public boolean puedeGestionar() {
        Usuario usuario = acceso.getUsuarioActual();
        return usuario.getPerfiles().contains(Perfil.RECEPCIONISTA)
                || usuario.getPerfiles().contains(Perfil.ADMINISTRADOR);
    }

    private String validar(String valor, String campo, int limite) {
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
