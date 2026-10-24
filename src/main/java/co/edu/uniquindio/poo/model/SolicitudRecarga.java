package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class SolicitudRecarga {
    private final String id = UUID.randomUUID().toString();
    private final Cliente cliente;
    private final double monto;
    private final LocalDateTime fecha = LocalDateTime.now();
    private EstadoSolicitudRecarga estado = EstadoSolicitudRecarga.PENDIENTE;

    public SolicitudRecarga(Cliente cliente, double monto) {
        if (cliente == null || monto <= 0 || !Double.isFinite(monto))
            throw new IllegalArgumentException("Cliente y monto válido son obligatorios.");
        this.cliente = cliente;
        this.monto = monto;
    }

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoSolicitudRecarga getEstado() {
        return estado;
    }

    void cambiarEstado(EstadoSolicitudRecarga estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return fecha.toLocalDate() + " · " + cliente.getNombre() + " · $" + String.format("%,.0f", monto) + " · " + estado;
    }
}
