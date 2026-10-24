package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Compra {
    private final String id = UUID.randomUUID().toString();

    private final Cliente cliente;
    private final Funcion funcion;
    private final List<Entrada> entradas = new ArrayList<>();
    private final List<DetalleCompra> detalles = new ArrayList<>();
    private final LocalDateTime fecha = LocalDateTime.now();
    private double total;
    private double descuentoEntradas;
    private boolean cancelada;
    private int puntosObtenidos;
    private String idMovimientoPuntos;
    private final java.util.Map<Producto, Integer> productosConsumidos = new java.util.LinkedHashMap<>();

    public Compra(Cliente c, Funcion f) {
        cliente = c;
        funcion = f;
    }

    public void agregarEntrada(Entrada e) {
        entradas.add(e);
    }

    public void agregarDetalle(DetalleCompra d) {
        detalles.add(d);
    }

    public double calcularTotal() {
        return entradas.stream().mapToDouble(Entrada::getValor).sum() + detalles.stream().mapToDouble(DetalleCompra::subtotal).sum() - descuentoEntradas;
    }

    public void confirmar(double t) {
        total = t;
    }

    public void confirmar(double t, double descuentoEntradas) {
        total = t;
        this.descuentoEntradas = descuentoEntradas;
    }

    public double getDescuentoEntradas() {
        return descuentoEntradas;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    void marcarCancelada() {
        cancelada = true;
    }

    void setMovimientoPuntos(MovimientoPuntos movimiento) {
        if (movimiento != null) {
            puntosObtenidos = movimiento.puntos();
            idMovimientoPuntos = movimiento.id();
        }
    }

    String getIdMovimientoPuntos() {
        return idMovimientoPuntos;
    }

    void registrarProductosConsumidos(java.util.Map<Producto, Integer> consumidos) {
        productosConsumidos.putAll(consumidos);
    }

    java.util.Map<Producto, Integer> getProductosConsumidos() {
        return java.util.Map.copyOf(productosConsumidos);
    }

    public int registrarAsistencia(String idEntrada) {
        if (cancelada) throw new IllegalStateException("Una compra cancelada no registra asistencia.");
        if (java.time.LocalDateTime.now().isBefore(funcion.getFechaHora().plusMinutes(funcion.getPelicula().getDuracionMinutos())))
            throw new IllegalStateException("La asistencia se registra después de finalizar la función.");
        Entrada entrada = entradas.stream().filter(e -> e.getId().equals(idEntrada)).findFirst().orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada en la compra."));
        entrada.marcarAsistida();
        cliente.getRegistroPuntos().recompensarAsistencia();
        return 20;
    }

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public List<Entrada> getEntradas() {
        return List.copyOf(entradas);
    }

    public List<DetalleCompra> getDetalles() {
        return List.copyOf(detalles);
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return fecha.toLocalDate() + " · " + funcion.getPelicula().getTitulo() + " · " + entradas.size() + " entrada(s) · $" + String.format("%,.0f", total) + (cancelada ? " · CANCELADA" : "");
    }
}
