package co.edu.uniquindio.poo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Sala {
    private final String idSala;
    private final TipoSala tipoSala;
    private final List<Asiento> asientos = new ArrayList<>();
    private final int filas;
    private final int columnas;

    public Sala(String idSala, int filas, int columnas) {
        this(idSala, filas, columnas, TipoSala.DOS_D);
    }

    public Sala(String idSala, int filas, int columnas, TipoSala tipoSala) {
        if (idSala == null || filas < 1 || columnas < 1) throw new IllegalArgumentException("Sala inválida");
        this.idSala = idSala;
        this.tipoSala = Objects.requireNonNull(tipoSala, "El tipo de sala es obligatorio.");
        this.filas = filas;
        this.columnas = columnas;
        for (int r = 0; r < filas; r++)
            for (int c = 1; c <= columnas; c++) {
                String fila = String.valueOf((char) ('A' + r));
                int primeraFilaVip = Math.max(0, filas - Math.max(2, filas / 3));
                TipoAsiento tipo = r >= primeraFilaVip ? TipoAsiento.VIP : TipoAsiento.GENERAL;
                asientos.add(new Asiento(fila + c, fila, c, tipo));
            }
    }

    public String getIdSala() {
        return idSala;
    }

    public TipoSala getTipoSala() { return tipoSala; }

    public int getFilas() { return filas; }

    public int getColumnas() { return columnas; }

    /** Copia la estructura de la sala con identificador nuevo y asientos disponibles independientes. */
    public Sala clonar(String nuevoId) {
        return new Sala(nuevoId, filas, columnas, tipoSala);
    }

    public int getCapacidad() {
        return asientos.size();
    }

    public List<Asiento> getAsientos() {
        return Collections.unmodifiableList(asientos);
    }

    @Override
    public String toString() {
        return idSala;
    }
}
