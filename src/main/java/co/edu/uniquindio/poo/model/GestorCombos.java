package co.edu.uniquindio.poo.model;

import java.util.ArrayList;
import java.util.List;

public class GestorCombos {
    private final List<Combo> combos;

    public GestorCombos(List<Combo> combos) { this.combos = new ArrayList<>(combos == null ? List.of() : combos); }
    public synchronized List<Combo> consultarCombos() { return List.copyOf(combos); }
    public synchronized void agregarCombo(Combo combo) {
        if (combo == null || combo.getComponentes().isEmpty())
            throw new IllegalArgumentException("El combo debe tener al menos un producto.");
        if (combos.stream().anyMatch(c -> c.getId().equals(combo.getId())))
            throw new IllegalArgumentException("Ya existe un combo con ese identificador.");
        combos.add(combo);
    }
}
