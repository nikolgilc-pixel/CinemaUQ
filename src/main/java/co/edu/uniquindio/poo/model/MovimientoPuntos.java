package co.edu.uniquindio.poo.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record MovimientoPuntos(String id, int puntos, LocalDate fechaObtencion) {
    public MovimientoPuntos {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El id del movimiento es obligatorio.");
        if (puntos < 0) throw new IllegalArgumentException("Los puntos no pueden ser negativos.");
        Objects.requireNonNull(fechaObtencion, "La fecha de obtención es obligatoria.");
    }

    public MovimientoPuntos(int puntos, LocalDate fechaObtencion) {
        this(UUID.randomUUID().toString(), puntos, fechaObtencion);
    }

    public LocalDate fechaVencimiento() {
        return fechaObtencion.plusYears(1);
    }

    public boolean estaVigente() {
        return !LocalDate.now().isAfter(fechaVencimiento());
    }

    public MovimientoPuntos conPuntos(int saldo) {
        return new MovimientoPuntos(id, saldo, fechaObtencion);
    }
}
