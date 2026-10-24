package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GestorCompra {
    private final IPagoTarjeta pago;
    private final List<Compra> compras = new ArrayList<>();

    public GestorCompra(IPagoTarjeta pago) {
        this.pago = Objects.requireNonNull(pago, "El servicio de pago es obligatorio.");
    }

    public synchronized Compra registrarCompra(Compra compra, List<Asiento> seleccion,
                                                Map<Producto, Integer> productos,
                                                Map<IComponenteConfiteria, Integer> combos) {
        if (compra == null || compra.getCliente() == null || compra.getFuncion() == null
                || seleccion == null || seleccion.isEmpty())
            throw new CompraInvalidaException("Selecciona una función y al menos un asiento.");
        if (!compra.getEntradas().isEmpty() || !compra.getDetalles().isEmpty() || compra.isCancelada()
                || compras.contains(compra))
            throw new CompraInvalidaException("La compra ya fue utilizada o no está en estado inicial.");
        if (new HashSet<>(seleccion).size() != seleccion.size())
            throw new CompraInvalidaException("Hay asientos repetidos.");
        for (Asiento asiento : seleccion) {
            if (asiento == null || !asiento.estaDisponible()
                    || !compra.getFuncion().getAsientos().contains(asiento))
                throw new AsientoNoDisponibleException("El asiento seleccionado no está disponible en esta función.");
        }

        Map<Producto, Integer> productosPedido = productos == null ? Map.of() : productos;
        Map<IComponenteConfiteria, Integer> combosPedido = combos == null ? Map.of() : combos;
        Map<Producto, Integer> consumo = new LinkedHashMap<>();
        for (Map.Entry<Producto, Integer> e : productosPedido.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue() < 1)
                throw new CompraInvalidaException("Producto o cantidad inválidos.");
            consumo.merge(e.getKey(), e.getValue(), Integer::sum);
        }
        for (Map.Entry<IComponenteConfiteria, Integer> e : combosPedido.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue() < 1)
                throw new CompraInvalidaException("Combo o cantidad inválidos.");
            acumularProductos(e.getKey(), e.getValue(), consumo);
        }
        for (Map.Entry<Producto, Integer> e : consumo.entrySet()) {
            if (e.getValue() < 1 || e.getKey().getStock() < e.getValue())
                throw new StockInsuficienteException("Stock insuficiente para " + e.getKey().getNombre());
        }

        List<Entrada> entradasPreparadas = new ArrayList<>();
        double subtotalEntradas = 0;
        for (Asiento asiento : seleccion) {
            Entrada entrada = new Entrada(UUID.randomUUID().toString(), compra.getFuncion(), asiento);
            entradasPreparadas.add(entrada);
            subtotalEntradas += entrada.getValor();
        }
        double descuentoEntradas = seleccion.size() >= 5 ? subtotalEntradas * 0.10 : 0;
        double total = subtotalEntradas - descuentoEntradas;
        List<DetalleCompra> detallesPreparados = new ArrayList<>();
        for (Map.Entry<Producto, Integer> e : productosPedido.entrySet()) {
            detallesPreparados.add(new DetalleCompra(e.getKey().getNombre(), e.getValue(), e.getKey().calcularPrecio()));
            total += e.getKey().calcularPrecio() * e.getValue();
        }
        for (Map.Entry<IComponenteConfiteria, Integer> e : combosPedido.entrySet()) {
            detallesPreparados.add(new DetalleCompra(e.getKey().getNombre(), e.getValue(), e.getKey().calcularPrecio()));
            total += e.getKey().calcularPrecio() * e.getValue();
        }
        if (!Double.isFinite(total) || total <= 0)
            throw new CompraInvalidaException("El total de la compra debe ser finito y mayor que cero.");

        // Si el pago falla, aún no se han modificado la compra, los asientos ni el inventario.
        pago.procesarPago(compra.getCliente().getTarjeta(), total);
        for (Asiento asiento : seleccion) asiento.ocupar();
        consumo.forEach(Producto::descontarStock);
        entradasPreparadas.forEach(compra::agregarEntrada);
        detallesPreparados.forEach(compra::agregarDetalle);
        compra.registrarProductosConsumidos(consumo);
        compra.confirmar(total, descuentoEntradas);
        MovimientoPuntos movimiento = compra.getCliente().getRegistroPuntos().acumularCompra(total);
        compra.setMovimientoPuntos(movimiento);
        compras.add(compra);
        return compra;
    }

    private void acumularProductos(IComponenteConfiteria componente, int cantidad,
                                   Map<Producto, Integer> consumo) {
        if (componente instanceof Producto producto) {
            consumo.merge(producto, cantidad, Integer::sum);
        } else if (componente instanceof Combo combo) {
            for (IComponenteConfiteria hijo : combo.getComponentes())
                acumularProductos(hijo, cantidad, consumo);
        } else if (componente instanceof ComboConPromocion decorador) {
            acumularProductos(decorador.getComponente(), cantidad, consumo);
        } else {
            throw new CompraInvalidaException("No se puede validar el inventario de este componente.");
        }
    }

    public synchronized Compra cancelarCompra(String id, Cliente cliente) {
        Compra compra = compras.stream().filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(() -> new CompraInvalidaException("No existe esa compra."));
        if (cliente == null || !compra.getCliente().getEmail().equalsIgnoreCase(cliente.getEmail()))
            throw new AccesoDenegadoException("La compra no pertenece a este cliente.");
        if (compra.isCancelada()) throw new CompraInvalidaException("La compra ya fue cancelada.");
        if (!LocalDateTime.now().isBefore(compra.getFuncion().getFechaHora()))
            throw new CompraInvalidaException("La compra solo puede cancelarse antes del inicio de la función.");

        compra.getCliente().getTarjeta().reembolsar(compra.getTotal());
        compra.getEntradas().forEach(e -> e.getAsiento().liberar());
        compra.getProductosConsumidos().forEach(Producto::devolverStock);
        compra.getCliente().getRegistroPuntos().revertirMovimiento(compra.getIdMovimientoPuntos());
        compra.marcarCancelada();
        return compra;
    }

    public synchronized List<Compra> comprasDe(Cliente cliente) {
        if (cliente == null) return List.of();
        return compras.stream().filter(c -> c.getCliente().getEmail().equalsIgnoreCase(cliente.getEmail())).toList();
    }

    public synchronized List<Compra> consultarCompras() { return List.copyOf(compras); }
}
