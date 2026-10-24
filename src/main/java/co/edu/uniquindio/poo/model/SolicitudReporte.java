package co.edu.uniquindio.poo.model;

import java.time.LocalDate;
import java.util.Objects;

public class SolicitudReporte {
    private final TipoReporte tipo;
    private final FormatoReporte formato;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    public SolicitudReporte(TipoReporte tipo, FormatoReporte formato, LocalDate fechaInicio, LocalDate fechaFin) {
        this.tipo = Objects.requireNonNull(tipo, "El tipo de reporte es obligatorio.");
        this.formato = Objects.requireNonNull(formato, "El formato de reporte es obligatorio.");
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio))
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial.");
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public TipoReporte getTipo() {
        return tipo;
    }

    public FormatoReporte getFormato() {
        return formato;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public boolean incluye(LocalDate fecha) {
        return (fechaInicio == null || !fecha.isBefore(fechaInicio))
                && (fechaFin == null || !fecha.isAfter(fechaFin));
    }
}
