package co.edu.uniquindio.poo.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BuscarFuncion implements IBusqueda {
    private final GestorFuncion gestorFuncion;

    public BuscarFuncion(GestorFuncion gestorFuncion) {
        this.gestorFuncion = Objects.requireNonNull(gestorFuncion, "El gestor de funciones es obligatorio.");
    }

    @Override
    public List<Funcion> buscarCriterio(FiltroBusqueda filtro, String valor) {
        Objects.requireNonNull(filtro, "El filtro de búsqueda es obligatorio.");
        String criterio = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
        if (criterio.isEmpty()) return List.of();
        List<Funcion> funciones = gestorFuncion.listarFunciones().stream().filter(funcion -> funcion.getFechaHora().isAfter(java.time.LocalDateTime.now())).toList();
        return funciones.stream().filter(funcion -> switch (filtro) {
            case POR_NOMBRE_PELICULA -> funcion.getPelicula().getTitulo().toLowerCase(Locale.ROOT).contains(criterio);
            case POR_GENERO -> funcion.getPelicula().getGenero().toLowerCase(Locale.ROOT).contains(criterio);
            case POR_HORARIO -> funcion.getFechaHora().toLocalTime().toString().contains(criterio);
            case POR_FECHA -> coincideFecha(funcion, criterio);
        }).toList();
    }

    private boolean coincideFecha(Funcion funcion, String criterio) {
        try {
            return funcion.getFechaHora().toLocalDate().equals(LocalDate.parse(criterio));
        } catch (DateTimeParseException e) {
            return funcion.getFechaHora().toLocalDate().toString().contains(criterio);
        }
    }
}
