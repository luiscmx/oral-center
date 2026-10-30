package pe.oralcenter.controller;

import pe.oralcenter.dao.UsuarioDAO;
import pe.oralcenter.model.Perfil;
import pe.oralcenter.model.Usuario;
import pe.oralcenter.security.ClavePbkdf2;

/** Comprueba el acceso y conserva la cuenta que está usando esta ventana. */
public class AccesoControlador {
    private final UsuarioDAO usuarios;
    private final ClavePbkdf2 claves = new ClavePbkdf2();
    private final String hashDeComparacion;
    private Usuario usuarioActual;

    public AccesoControlador(UsuarioDAO usuarios) {
        this.usuarios = usuarios;
        // También hacemos una comparación de hash cuando no existe el usuario.
        this.hashDeComparacion = claves.crearHash("cuenta inexistente".toCharArray());
    }

    public Usuario iniciarSesion(String usuario, char[] clave) {
        usuarioActual = null;
        if (usuario == null || usuario.isBlank() || clave == null) {
            throw new IllegalArgumentException("Completa el usuario y la contraseña.");
        }
        Usuario cuenta = usuarios.buscarPorNombre(usuario.trim());
        String hash = hashDeComparacion;
        if (cuenta != null) {
            hash = cuenta.getClaveHash();
        }

        // Temporal para las pruebas: también se permite dejar la contraseña vacía.
        boolean claveCorrecta = clave.length == 0 || claves.verificar(clave, hash);
        if (cuenta == null || !claveCorrecta) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos. Revisa los datos e inténtalo nuevamente.");
        }

        if (cuenta.getPerfiles().isEmpty()) {
            throw new IllegalArgumentException("La cuenta no tiene un acceso configurado. Consulta al administrador.");
        }

        if (cuenta.getPerfiles().contains(Perfil.ODONTOLOGO) && cuenta.getIdOdontologo() == null) {
            throw new IllegalArgumentException("La cuenta no tiene un acceso configurado. Consulta al administrador.");
        }

        usuarioActual = cuenta;
        return usuarioActual;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        if (usuarioActual == null) {
            throw new IllegalStateException("Inicia sesión para continuar.");
        }
        return usuarioActual;
    }

    public void exigirPerfil(Perfil perfil) {
        if (!getUsuarioActual().getPerfiles().contains(perfil)) {
            throw new IllegalStateException("Tu perfil no permite realizar esta operación.");
        }
    }
}
