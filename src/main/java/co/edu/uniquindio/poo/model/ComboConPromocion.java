package co.edu.uniquindio.poo.model;

import java.time.LocalDate;

public class ComboConPromocion implements IComponenteConfiteria {
    private final IComponenteConfiteria componente;
    private final Promocion promocion;

    public ComboConPromocion(IComponenteConfiteria c, Promocion p) {
        componente = c;
        promocion = p;
    }

    public String getNombre() {
        return componente.getNombre() + " (" + promocion.nombre() + ")";
    }

    public double calcularPrecio() {
        return promocion.estaVigente(LocalDate.now()) ? promocion.aplicar(componente.calcularPrecio()) : componente.calcularPrecio();
    }

    public IComponenteConfiteria getComponente() {
        return componente;
    }

    public Promocion getPromocion() {
        return promocion;
    }

    public String toString() {
        return getNombre() + " ($" + String.format("%,.0f", calcularPrecio()) + ")";
    }
}
