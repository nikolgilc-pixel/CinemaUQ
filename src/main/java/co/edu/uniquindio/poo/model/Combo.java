package co.edu.uniquindio.poo.model;

import java.util.ArrayList;
import java.util.List;

public class Combo implements IComponenteConfiteria {
    private final String id, nombre;
    private final List<IComponenteConfiteria> componentes = new ArrayList<>();

    public Combo(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<IComponenteConfiteria> getComponentes() {
        return List.copyOf(componentes);
    }

    public void agregarComponente(IComponenteConfiteria c) {
        componentes.add(c);
    }

    public double calcularPrecio() {
        return componentes.stream().mapToDouble(IComponenteConfiteria::calcularPrecio).sum();
    }

    public String toString() {
        return nombre + " ($" + String.format("%,.0f", calcularPrecio()) + ")";
    }
}
