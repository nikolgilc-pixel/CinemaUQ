package co.edu.uniquindio.poo.model;

public final class DescuentoPorCantidadEntradas implements IEstrategiaDescuento {
    private final int minimoEntradas;
    private final double porcentaje;

    public DescuentoPorCantidadEntradas() {
        this(5, 10);
    }

    public DescuentoPorCantidadEntradas(int minimoEntradas, double porcentaje) {
        if (minimoEntradas < 1 || !Double.isFinite(porcentaje) || porcentaje < 0 || porcentaje > 100)
            throw new IllegalArgumentException("La política de descuento es inválida.");
        this.minimoEntradas = minimoEntradas;
        this.porcentaje = porcentaje;
    }

    @Override
    public double calcularDescuento(double subtotalEntradas, int cantidadEntradas) {
        if (!Double.isFinite(subtotalEntradas) || subtotalEntradas < 0)
            throw new IllegalArgumentException("El subtotal de entradas no es válido.");
        return cantidadEntradas >= minimoEntradas ? subtotalEntradas * porcentaje / 100 : 0;
    }
}
