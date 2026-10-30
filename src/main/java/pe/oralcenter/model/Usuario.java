package pe.oralcenter.model;

import java.util.Set;
import com.google.common.collect.ImmutableSet;

/** Datos de acceso correspondientes a usuario, perfil y usuario_perfil del modelo. */
public class Usuario {
    private final long id;
    private final String nombreUsuario;
    private final String claveHash;
    private final Set<Perfil> perfiles;
    private final Long idOdontologo;

    public Usuario(long id, String nombreUsuario, String claveHash,
                   Set<Perfil> perfiles, Long idOdontologo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.claveHash = claveHash;
        // La copia evita que otra parte del programa modifique los permisos.
        this.perfiles = ImmutableSet.copyOf(perfiles);
        this.idOdontologo = idOdontologo;
    }

    public long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public Set<Perfil> getPerfiles() {
        return perfiles;
    }

    public Long getIdOdontologo() {
        return idOdontologo;
    }
}
