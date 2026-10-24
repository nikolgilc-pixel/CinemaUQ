package co.edu.uniquindio.poo.model;

public class PagoTarjetaVirtual implements IPagoTarjeta {
    public void procesarPago(TarjetaVirtual t, double m) {
        t.procesarCompra(m);
    }
}
