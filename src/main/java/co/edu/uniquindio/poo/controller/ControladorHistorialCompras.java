package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.model.Compra;
import co.edu.uniquindio.poo.model.IObservadorCompra;

import java.util.List;
import java.util.Objects;

/** Operaciones del historial del cliente, separadas del panel administrativo. */
public final class ControladorHistorialCompras implements IObservadorCompra {
    private final CinemaController cine;
    private List<Compra> compras = List.of();
    private Runnable alCambiar = () -> { };

    public ControladorHistorialCompras(CinemaController cine) {
        this.cine = Objects.requireNonNull(cine);
        refrescar();
    }

    public List<Compra> consultarCompras() {
        return compras;
    }

    public Compra cancelarCompra(String idCompra) {
        return cine.cancelarCompra(idCompra);
    }

    public void setAlCambiar(Runnable alCambiar) {
        this.alCambiar = Objects.requireNonNull(alCambiar);
    }

    private void refrescar() {
        compras = cine.comprasDelCliente();
    }

    @Override
    public void actualizar(Compra compra) {
        if (cine.cliente() != null && compra.getCliente() == cine.cliente()) {
            refrescar();
            alCambiar.run();
        }
    }
}
