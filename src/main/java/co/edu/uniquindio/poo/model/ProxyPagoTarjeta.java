package co.edu.uniquindio.poo.model;

public class ProxyPagoTarjeta implements IPagoTarjeta {
    private final PagoTarjetaVirtual pagoReal;

    public ProxyPagoTarjeta(PagoTarjetaVirtual p) {
        pagoReal = p;
    }

    public void procesarPago(TarjetaVirtual t, double m) {
        if (t == null || !t.estaActiva()) throw new IllegalStateException("La tarjeta no está activa");
        if (t.getSaldo() < m)
            throw new SaldoInsuficienteException("Saldo insuficiente. Saldo: $" + t.getSaldo());
        pagoReal.procesarPago(t, m);
    }
}
