package co.edu.uniquindio.poo.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GestorSala {
    private final Map<String, Sala> salas = new LinkedHashMap<>();

    public synchronized Sala registrar(String id, int filas, int columnas, TipoSala tipo) {
        if (salas.containsKey(id)) throw new IllegalArgumentException("Ya existe una sala con ese nombre.");
        Sala sala = new Sala(id, filas, columnas, tipo);
        salas.put(id, sala);
        return sala;
    }

    public synchronized Sala registrar(Sala sala) {
        if (sala == null) throw new IllegalArgumentException("La sala es obligatoria.");
        if (salas.putIfAbsent(sala.getIdSala(), sala) != null)
            throw new IllegalArgumentException("Ya existe una sala con ese nombre.");
        return sala;
    }

    public synchronized Sala crearDesdePlantilla(String nuevoId, String idPlantilla) {
        Sala plantilla = salas.get(idPlantilla);
        if (plantilla == null) throw new IllegalArgumentException("No existe la sala plantilla.");
        Sala copia = plantilla.clonar(nuevoId);
        return registrar(copia);
    }

    public synchronized Sala registrarSiNoExiste(String id, int filas, int columnas, TipoSala tipo) {
        Sala existente = salas.get(id);
        if (existente != null) {
            if (existente.getCapacidad() != filas * columnas || existente.getTipoSala() != tipo)
                throw new IllegalArgumentException("La sala ya existe con otro tipo o capacidad.");
            return existente;
        }
        return registrar(id, filas, columnas, tipo);
    }

    public synchronized List<Sala> listar() {
        return List.copyOf(salas.values());
    }

    public synchronized boolean eliminar(String id) {
        return salas.remove(id) != null;
    }
}
