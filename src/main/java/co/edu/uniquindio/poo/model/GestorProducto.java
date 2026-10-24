package co.edu.uniquindio.poo.model;

import java.util.ArrayList;
import java.util.List;

public class GestorProducto {
    private final List<Producto> productos;

    public GestorProducto(List<Producto> productos) {
        this.productos = new ArrayList<>(productos);
    }

    public synchronized List<Producto> consultarProductos() {
        return List.copyOf(productos);
    }

    public synchronized void agregarProducto(Producto producto) {
        if (producto == null) throw new IllegalArgumentException("El producto no puede estar vacío.");
        if (productos.stream().anyMatch(p -> p.getId().equals(producto.getId())))
            throw new IllegalArgumentException("Ya existe un producto con ese identificador.");
        productos.add(producto);
    }
}
