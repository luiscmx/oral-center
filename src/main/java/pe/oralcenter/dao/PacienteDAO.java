package pe.oralcenter.dao;

import java.util.List;
import pe.oralcenter.model.Paciente;

/** Operaciones de pacientes disponibles en este bloque. */
public interface PacienteDAO {
    void guardar(Paciente paciente);
    void actualizar(Paciente paciente);
    void eliminar(long id);
    List<Paciente> listar();
    List<Paciente> buscar(String texto);
    List<Paciente> buscarAsociados(String texto, long odontologo);
}
