package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestorFuncion {
    private final List<Funcion> funciones;

    public GestorFuncion(List<Funcion> funciones) {
        this.funciones = new ArrayList<>(funciones);
    }

    public synchronized List<Funcion> listarFunciones() {
        return List.copyOf(funciones);
    }

    public synchronized List<Pelicula> listarPeliculas() {
        return funciones.stream().map(Funcion::getPelicula).distinct().toList();
    }

    public synchronized List<Funcion> funcionesDe(Pelicula pelicula) {
        return funciones.stream().filter(f -> f.getPelicula().equals(pelicula) && f.getFechaHora().isAfter(LocalDateTime.now())).toList();
    }

    public synchronized void programarFuncion(Funcion funcion) {
        if (funcion == null)
            throw new IllegalArgumentException("La función no puede estar vacía.");
        if (funciones.stream().anyMatch(f -> f.getId().equals(funcion.getId())))
            throw new IllegalArgumentException("Ya existe una función con ese identificador.");
        funciones.add(funcion);
    }
}
