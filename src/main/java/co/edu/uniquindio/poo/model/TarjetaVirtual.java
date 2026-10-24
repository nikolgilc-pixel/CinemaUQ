package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TarjetaVirtual {
    private final String numero;
    private double saldo;
    private EstadoTarjeta estado = EstadoTarjeta.ACTIVA;
    private java.time.LocalDateTime bloqueadaHasta;
    private final List<MovimientoTarjeta> movimientos = new ArrayList<>();

    public TarjetaVirtual(String numero, double saldoInicial) {
        if (numero == null || numero.isBlank()) throw new IllegalArgumentException("El número de tarjeta es obligatorio.");
        if (!Double.isFinite(saldoInicial) || saldoInicial < 0) throw new IllegalArgumentException("El saldo inicial debe ser finito y no negativo.");
        this.numero = numero.trim();
        saldo = saldoInicial;
    }

    public String getNumero() {
        return numero;
    }

    public double getSaldo() {
        return saldo;
    }

    public EstadoTarjeta getEstado() {
        return estado;
    }

    public boolean estaActiva() {
        if (estado == EstadoTarjeta.BLOQUEADA && bloqueadaHasta != null && !java.time.LocalDateTime.now().isBefore(bloqueadaHasta)) {
            estado = EstadoTarjeta.ACTIVA;
            bloqueadaHasta = null;
        }
        return estado == EstadoTarjeta.ACTIVA;
    }

    public List<MovimientoTarjeta> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    public void bloquear() {
        estado = EstadoTarjeta.BLOQUEADA;
        bloqueadaHasta = null;
    }

    public void bloquearTemporalmente(java.time.Duration duracion) {
        if (duracion == null || duracion.isNegative() || duracion.isZero())
            throw new IllegalArgumentException("Duración de bloqueo inválida.");
        estado = EstadoTarjeta.BLOQUEADA;
        bloqueadaHasta = java.time.LocalDateTime.now().plus(duracion);
    }

    public void reembolsar(double monto) {
        if (!Double.isFinite(monto) || monto <= 0)
            throw new IllegalArgumentException("El reembolso debe ser positivo.");
        saldo += monto;
        registrar("REEMBOLSO", monto);
    }

    public void acreditarCanje(double monto) {
        if (!Double.isFinite(monto) || monto <= 0) throw new IllegalArgumentException("El canje debe ser positivo.");
        saldo += monto;
        registrar("CANJE_PUNTOS", monto);
    }

    public void recargar(double monto) {
        if (!Double.isFinite(monto) || monto <= 0)
            throw new IllegalArgumentException("La recarga debe ser finita y positiva.");
        saldo += monto;
        registrar("RECARGA", monto);
    }

    public void procesarCompra(double monto) {
        if (!estaActiva()) throw new IllegalStateException("La tarjeta está bloqueada.");
        if (monto <= 0) throw new IllegalArgumentException("El pago debe ser positivo.");
        if (monto > saldo)
            throw new SaldoInsuficienteException("Saldo insuficiente.");
        saldo -= monto;
        registrar("COMPRA", monto);
    }

    private void registrar(String tipo, double monto) {
        movimientos.add(new MovimientoTarjeta(tipo, monto, saldo, LocalDateTime.now()));
    }
}
