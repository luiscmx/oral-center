package pe.oralcenter.dao;

import java.util.List;
import pe.oralcenter.model.Horario;
import pe.oralcenter.model.Odontologo;

public interface OdontologoDAO {
    void guardar(Odontologo odontologo);
    void actualizar(Odontologo odontologo);
    List<Odontologo> listar();
    void guardarHorario(Horario horario);
    List<Horario> listarHorarios(long idOdontologo);
}
