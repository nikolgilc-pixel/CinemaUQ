package co.edu.uniquindio.poo.model;

public class Producto implements IComponenteConfiteria {
    private final String id, nombre;
    private final double precio;
    private int stock;

    public Producto(String id, String nombre, double precio, int stock) {
        if (id == null || nombre == null || precio < 0 || stock < 0)
            throw new IllegalArgumentException("Producto inválido");
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getStock() {
        return stock;
    }

    public void abastecer(int cantidad) {
        if (cantidad < 1) throw new IllegalArgumentException("La cantidad debe ser positiva");
        stock += cantidad;
    }

    public void devolverStock(int cantidad) { if (cantidad < 1) throw new IllegalArgumentException("Cantidad inválida."); stock += cantidad; }

    public void descontarStock(int n) {
        if (n < 1 || n > stock) throw new StockInsuficienteException(nombre + ": stock insuficiente");
        stock -= n;
    }

    public double calcularPrecio() {
        return precio;
    }

    public String toString() {
        return nombre + " ($" + String.format("%,.0f", precio) + ") · stock " + stock;
    }
}
