package co.edu.uniquindio.poo.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class GestorUsuario {
    private final Map<String, CuentaCliente> clientes = new HashMap<>();
    private final Administrador administrador;
    private final GestorTarjeta gestorTarjeta;
    private final String claveAdministrador = "Admin123!";

    public GestorUsuario(Cliente clienteDemo) {
        this(clienteDemo, new GestorTarjeta());
        gestorTarjeta.registrar(clienteDemo.getTarjeta());
    }

    public GestorUsuario(Cliente clienteDemo, GestorTarjeta gestorTarjeta) {
        this.gestorTarjeta = java.util.Objects.requireNonNull(gestorTarjeta);
        clientes.put(normalizar(clienteDemo.getEmail()), new CuentaCliente(clienteDemo, "Cliente123!"));
        administrador = new Administrador("ADM-001", "Mañana", "Administrador CinemaUQ", "admin@cinemauq.edu.co");
    }

    public synchronized Cliente autenticarCliente(String correo, String clave) {
        CuentaCliente cuenta = clientes.get(normalizar(correo));
        if (cuenta == null && correo != null) {
            cuenta = clientes.values().stream()
                    .filter(c -> c.cliente().getNombre().equalsIgnoreCase(correo.trim()))
                    .findFirst().orElse(null);
        }
        if (cuenta != null) cuenta.verificarBloqueo();
        if (cuenta == null || !cuenta.clave().equals(clave)) {
            if (cuenta != null) cuenta.fallo();
            throw new AccesoDenegadoException("Correo o contraseña incorrectos.");
        }
        cuenta.exito(); return cuenta.cliente();
    }

    public synchronized Cliente registrarCliente(String nombre, String correo, String clave) {
        if (nombre == null || nombre.isBlank() || correo == null || !correo.contains("@") || clave == null || clave.length() < 6)
            throw new IllegalArgumentException("Escribe un nombre, correo válido y una contraseña de al menos 6 caracteres.");
        String llave = normalizar(correo);
        if (clientes.containsKey(llave))
            throw new CuentaExistenteException("Ya existe una cuenta con ese correo.");
        String numeroTarjeta = "UQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        Cliente nuevo = new Cliente(nombre.trim(), correo.trim(), new TarjetaVirtual(numeroTarjeta, 50000));
        gestorTarjeta.registrar(nuevo.getTarjeta());
        clientes.put(llave, new CuentaCliente(nuevo, clave));
        return nuevo;
    }

    public synchronized java.util.List<Cliente> listarClientes() {
        return clientes.values().stream().map(CuentaCliente::cliente).toList();
    }

    public boolean autenticarAdministrador(String correo, String clave) {
        return correo != null && clave != null
                && (correo.trim().equalsIgnoreCase(administrador.getEmail())
                || correo.trim().equalsIgnoreCase("admin"))
                && clave.equals(claveAdministrador);
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    private String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private static final class CuentaCliente {
        private final Cliente cliente; private final String clave;
        private int intentosFallidos; private java.time.LocalDateTime bloqueadaHasta;
        private CuentaCliente(Cliente cliente, String clave) { this.cliente=cliente; this.clave=clave; }
        Cliente cliente() { return cliente; } String clave() { return clave; }
        void verificarBloqueo() { if (bloqueadaHasta != null && java.time.LocalDateTime.now().isBefore(bloqueadaHasta)) throw new AccesoDenegadoException("Cuenta bloqueada temporalmente hasta "+bloqueadaHasta+"."); if (bloqueadaHasta != null) { bloqueadaHasta=null; intentosFallidos=0; } }
        void fallo() { if (++intentosFallidos >= 3) { bloqueadaHasta=java.time.LocalDateTime.now().plusMinutes(5); intentosFallidos=0; } }
        void exito() { intentosFallidos=0; }
    }
}
