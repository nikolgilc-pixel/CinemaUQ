package co.edu.uniquindio.poo.model;

public record DetalleCompra(String descripcion, int cantidad, double precioUnitario) {
    public DetalleCompra {
        if (cantidad < 1 || precioUnitario < 0)
            throw new IllegalArgumentException("Detalle inválido");
    }

    public double subtotal() {
        return cantidad * precioUnitario;
    }
}
