package pe.oralcenter.model;

/** Perfiles acordados para Oral Center. Una cuenta puede reunir varios. */
public enum Perfil {
    RECEPCIONISTA("Recepcionista del consultorio"),
    ODONTOLOGO("Odontólogo"),
    ADMINISTRADOR("Administrador");

    private final String etiqueta;
    Perfil(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
