package co.edu.uniquindio.poo.model;

public class EstadoCompraCancelada implements IEstadoCompra{
    @Override
    public boolean puedeCancelar() {
        return false;
    }

    @Override
    public void cancelar(Compra compra) {
        throw new CompraInvalidaException("La compra ya fue cancelar");
    }
}
