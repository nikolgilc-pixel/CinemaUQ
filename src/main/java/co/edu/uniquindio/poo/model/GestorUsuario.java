package co.edu.uniquindio.poo.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class GestorUsuario {
    private final Map<String, CuentaCliente> clientes = new HashMap<>();
    private final String correoAdministrador = "admin@cinemauq.edu.co";
    private final String claveAdministrador = "Admin123!";
    private final Administrador administrador;

    public GestorUsuario(Cliente clienteDemo) {
        this(clienteDemo, null);
    }

    public GestorUsuario(Cliente clienteDemo, GestorTarjeta gestorTarjeta) {
        if (clienteDemo == null) throw new IllegalArgumentException("El cliente inicial es obligatorio.");
        clientes.put(normalizar(clienteDemo.getEmail()), new CuentaCliente(clienteDemo, "Cliente123!"));
        if (gestorTarjeta != null && gestorTarjeta.buscar(clienteDemo.getTarjeta().getNumero()) == null)
            gestorTarjeta.registrar(clienteDemo.getTarjeta());
        administrador = new Administrador("ADMIN-001", "General", "Administrador CinemaUQ", correoAdministrador);
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public synchronized Cliente autenticarCliente(String correo, String clave) {
        CuentaCliente cuenta = clientes.get(normalizar(correo));
        if (cuenta == null && correo != null) {
            cuenta = clientes.values().stream()
                    .filter(c -> c.cliente().getNombre().equalsIgnoreCase(correo.trim()))
                    .findFirst().orElse(null);
        }
        if (cuenta == null || !cuenta.clave().equals(clave))
            throw new AccesoDenegadoException("Correo o contraseña incorrectos.");
        return cuenta.cliente();
    }

    public synchronized Cliente registrarCliente(String nombre, String correo, String clave) {
        if (nombre == null || nombre.isBlank() || correo == null || !correo.contains("@") || clave == null || clave.length() < 6)
            throw new IllegalArgumentException("Escribe un nombre, correo válido y una contraseña de al menos 6 caracteres.");
        String llave = normalizar(correo);
        if (clientes.containsKey(llave))
            throw new CuentaExistenteException("Ya existe una cuenta con ese correo.");
        String numeroTarjeta = "UQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        Cliente nuevo = new Cliente(nombre.trim(), correo.trim(), new TarjetaVirtual(numeroTarjeta, 50000));
        clientes.put(llave, new CuentaCliente(nuevo, clave));
        return nuevo;
    }

    public synchronized java.util.List<Cliente> listarClientes() {
        return clientes.values().stream().map(CuentaCliente::cliente).toList();
    }

    public boolean autenticarAdministrador(String correo, String clave) {
        return correo != null && clave != null && (correo.trim().equalsIgnoreCase(correoAdministrador) || correo.trim().equalsIgnoreCase("admin")) && clave.equals(claveAdministrador);
    }

    private String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private record CuentaCliente(Cliente cliente, String clave) {
    }
}
