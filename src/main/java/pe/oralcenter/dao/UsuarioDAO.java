package pe.oralcenter.dao;

import pe.oralcenter.model.Usuario;

/** Contrato de acceso a cuentas, independiente de MySQL y de la interfaz gráfica. */
public interface UsuarioDAO {
    /** Busca la cuenta y devuelve null cuando no existe. */
    Usuario buscarPorNombre(String nombreUsuario);
}
