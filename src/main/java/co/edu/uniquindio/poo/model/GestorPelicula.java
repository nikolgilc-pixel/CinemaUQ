package co.edu.uniquindio.poo.model;

import java.util.*;

public class GestorPelicula {
    private final Map<String, Pelicula> peliculas = new LinkedHashMap<>();

    public GestorPelicula() {
    }

    public GestorPelicula(Collection<Pelicula> iniciales) {
        if (iniciales != null) iniciales.forEach(this::registrarSiNoExiste);
    }

    public synchronized Pelicula registrar(Pelicula pelicula) {
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        if (peliculas.containsKey(pelicula.getId()))
            throw new IllegalArgumentException("Ya existe una película con ese identificador.");
        peliculas.put(pelicula.getId(), pelicula);
        return pelicula;
    }

    public synchronized Pelicula registrarSiNoExiste(Pelicula pelicula) {
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        return peliculas.computeIfAbsent(pelicula.getId(), id -> pelicula);
    }

    public synchronized Pelicula buscar(String id) {
        return peliculas.get(id);
    }

    public synchronized List<Pelicula> listar() {
        return List.copyOf(peliculas.values());
    }

    public synchronized boolean actualizar(Pelicula pelicula) {
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        if (!peliculas.containsKey(pelicula.getId())) return false;
        peliculas.put(pelicula.getId(), pelicula);
        return true;
    }

    public synchronized boolean eliminar(String id) {
        return peliculas.remove(id) != null;
    }
}
