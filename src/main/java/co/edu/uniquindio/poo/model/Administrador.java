package co.edu.uniquindio.poo.model;

public final class Administrador extends Usuario {
    private final String codigoAdmin;
    private final String turno;

    public Administrador(String codigoAdmin, String turno, String nombre, String email) {
        super(nombre, email);
        if (codigoAdmin == null || codigoAdmin.isBlank())
            throw new IllegalArgumentException("El código del administrador es obligatorio.");
        if (turno == null || turno.isBlank())
            throw new IllegalArgumentException("El turno del administrador es obligatorio.");
        this.codigoAdmin = codigoAdmin.trim();
        this.turno = turno.trim();
    }

    public String getCodigoAdmin() {
        return codigoAdmin;
    }

    public String getTurno() {
        return turno;
    }

    @Override
    public String getTipoUsuario() {
        return "Administrador";
    }
}
