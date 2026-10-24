package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Funcion {
    private final String id;
    private final Sala sala;
    private final Pelicula pelicula;
    private final LocalDateTime fechaHora;
    private final double precioBase;

    public Funcion(String id, Pelicula pelicula, String sala, LocalDateTime fechaHora, double precioBase, int filas, int columnas) {
        this(id, pelicula, sala, TipoSala.DOS_D, fechaHora, precioBase, filas, columnas);
    }

    public Funcion(String id, Pelicula pelicula, String sala, TipoSala tipoSala, LocalDateTime fechaHora, double precioBase, int filas, int columnas) {
        this.id = id;
        this.pelicula = pelicula;
        this.sala = new Sala(sala, filas, columnas, tipoSala);
        this.fechaHora = fechaHora;
        this.precioBase = precioBase;
    }

    private Funcion(String id, Pelicula pelicula, Sala sala, LocalDateTime fechaHora, double precioBase) {
        this.id = id;
        this.pelicula = pelicula;
        this.sala = sala;
        this.fechaHora = fechaHora;
        this.precioBase = precioBase;
    }

    /**
     * Copia la función y el mapa de asientos con identificadores y horario nuevos.
     */
    public Funcion clonar(String nuevoId, String nuevaSala, LocalDateTime nuevoHorario) {
        if (nuevoId == null || nuevoId.isBlank() || nuevaSala == null || nuevaSala.isBlank() || nuevoHorario == null)
            throw new IllegalArgumentException("El id, la sala y el horario de la copia son obligatorios.");
        return new Funcion(nuevoId, pelicula, sala.clonar(nuevaSala), nuevoHorario, precioBase);
    }

    public String getId() {
        return id;
    }

    public String getSala() {
        return sala.getIdSala();
    }

    public Sala getSalaModelo() {
        return sala;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public List<Asiento> getAsientos() {
        return sala.getAsientos();
    }

    public double calcularPrecio(Asiento a) {
        return precioBase + (a.getTipo() == TipoAsiento.VIP ? 6000 : 0);
    }

    public String getFechaLegible() {
        return fechaHora.format(DateTimeFormatter.ofPattern("EEE d MMM · HH:mm"));
    }

    @Override
    public String toString() {
        return pelicula.getTitulo() + " · " + getFechaLegible() + " · " + sala;
    }
}
