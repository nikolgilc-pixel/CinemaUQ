package co.edu.uniquindio.poo.model;

import java.time.LocalDate;

public record Promocion (String nombre, double porcentajeDescuento, LocalDate fechaInicio, LocalDate fechaFin) {
    public Promocion {
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100 || fechaFin.isBefore(fechaInicio))
            throw new IllegalArgumentException("Promoción inválida");
    }

    public boolean estaVigente(LocalDate f) {
        return !f.isBefore(fechaInicio) && !f.isAfter(fechaFin);
    }

    public double aplicar(double precio) {
        return precio * (1 - porcentajeDescuento / 100);
    }
}
