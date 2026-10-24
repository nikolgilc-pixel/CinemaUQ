package co.edu.uniquindio.poo.model;

public abstract class Usuario {
    private final String nombre;
    private final String email;

    protected Usuario(String nombre, String email) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del usuario es obligatorio.");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El correo del usuario es obligatorio.");
        this.nombre = nombre.trim();
        this.email = email.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public abstract String getTipoUsuario();
}
