package co.edu.uniquindio.poo.model;

import java.time.LocalDate;
import java.util.UUID;

public class MoviemientoPuntos(String id, int puntos, LocalDate fechaObtencion) {
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
