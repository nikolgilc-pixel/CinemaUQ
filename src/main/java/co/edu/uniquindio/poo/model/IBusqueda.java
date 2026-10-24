package co.edu.uniquindio.poo.model;

import java.util.List;

public interface IBusqueda {
    List<Funcion> buscarCriterio(FiltroBusqueda filtro, String valor);

}
