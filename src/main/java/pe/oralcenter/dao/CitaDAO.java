package pe.oralcenter.dao;

import java.time.LocalDate;
import java.util.List;
import pe.oralcenter.model.*;

public interface CitaDAO {
    List<Paciente> listarPacientes(Long odontologoPermitido);
    List<Odontologo> listarOdontologos(Long odontologoPermitido);
    List<TipoAtencion> listarTiposAtencion();
    List<Intervalo> consultarDisponibilidad(long odontologo, LocalDate fecha, int duracion);
    long guardar(Cita cita, Long odontologoPermitido);
    List<CitaAgenda> consultarAgenda(LocalDate fecha, Long odontologo, String estado);
}
