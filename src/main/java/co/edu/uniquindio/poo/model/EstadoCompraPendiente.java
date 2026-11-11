package co.edu.uniquindio.poo.model;
public final class EstadoCompraPendiente implements IEstadoCompra {
        @Override
        public boolean puedeCancelar() {
            return false;
        }

        @Override
        public void cancelar(Compra compra) {
            throw new CompraInvalidaException("No se puede cancelar una compra que aún no ha sido confirmada.");
        }

    }

