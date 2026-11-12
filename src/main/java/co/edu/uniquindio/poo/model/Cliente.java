package co.edu.uniquindio.poo.model;

public class Cliente extends Usuario {
    private final TarjetaVirtual tarjeta;
    private final RegistroPuntos registroPuntos = new RegistroPuntos();

    public Cliente(String nombre, String email, TarjetaVirtual tarjeta) {
        super(nombre, email);
        if (tarjeta == null) throw new IllegalArgumentException("El cliente debe tener una tarjeta virtual.");
        this.tarjeta = tarjeta;
    }

    public TarjetaVirtual getTarjeta() {
        return tarjeta;
    }

    public RegistroPuntos getRegistroPuntos() {
        return registroPuntos;
    }

    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }
}
