package co.edu.uniquindio.poo.model;

public class EstadoCompraConfirmada implements IEstadoCompra {
    @Override
    public boolean puedeCancelar() {
        return true;
    }

    @Override
    public void cancelar(Compra compra) {
        compra.marcarCancelada();
    }
}


