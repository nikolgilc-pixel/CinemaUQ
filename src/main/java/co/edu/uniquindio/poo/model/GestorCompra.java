package co.edu.uniquindio.poo.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GestorCompra {
    private final IPagoTarjeta pago;
    private final IEstrategiaDescuento estrategiaDescuento;
    private final IPoliticaReembolso politicaReembolso;
    private final List<Compra> compras = new ArrayList<>();
    private final List<IObservadorCompra> observadores = new CopyOnWriteArrayList<>();

    public GestorCompra(IPagoTarjeta pago) {
        this(pago, new DescuentoPorCantidadEntradas(), new ReembolsoPorTiempo());
    }

    public GestorCompra(IPagoTarjeta pago, IEstrategiaDescuento estrategiaDescuento) {
        this(pago, estrategiaDescuento, new ReembolsoPorTiempo());
    }

    public GestorCompra(IPagoTarjeta pago, IEstrategiaDescuento estrategiaDescuento,
                        IPoliticaReembolso politicaReembolso) {
        this.pago = Objects.requireNonNull(pago, "El servicio de pago es obligatorio.");
        this.estrategiaDescuento = Objects.requireNonNull(estrategiaDescuento, "La estrategia de descuento es obligatoria.");
        this.politicaReembolso = Objects.requireNonNull(politicaReembolso, "La política de reembolso es obligatoria.");
    }

    public void agregarObservador(IObservadorCompra observador) {
        observadores.add(Objects.requireNonNull(observador, "El observador es obligatorio."));
    }

    public void quitarObservador(IObservadorCompra observador) {
        observadores.remove(observador);
    }

    private void notificar(Compra compra) {
        for (IObservadorCompra observador : observadores) {
            try {
                observador.actualizar(compra);
            } catch (RuntimeException ex) {
                System.getLogger(GestorCompra.class.getName()).log(System.Logger.Level.ERROR,
                        "Falló un observador de compras; la transacción ya fue aplicada.", ex);
            }
        }
    }

    public synchronized Compra registrarCompra(Compra compra, List<Asiento> seleccion, Map<Producto,
            Integer> productos, Map<IComponenteConfiteria, Integer> combos) {

        if (compra == null || compra.getCliente() == null || compra.getFuncion() == null || seleccion == null || seleccion.isEmpty())
            throw new CompraInvalidaException("Selecciona función y al menos un asiento.");

        if (!compra.estaPendiente() || !compra.getEntradas().isEmpty() || !compra.getDetalles().isEmpty()
                || compras.contains(compra))
            throw new CompraInvalidaException("La compra ya fue utilizada o no está pendiente.");

        if (new HashSet<>(seleccion).size() != seleccion.size())
            throw new CompraInvalidaException("Hay asientos repetidos.");
        for (Asiento a : seleccion)
            if (!a.estaDisponible() || !compra.getFuncion().getAsientos().contains(a))
                throw new AsientoNoDisponibleException("El asiento " + a + " ya no está disponible.");
        Map<Producto, Integer> consumo = new LinkedHashMap<>(productos);
        combos.forEach((c, n) -> acumularProductos(c, n, consumo));
        consumo.forEach((p, n) -> {
            if (n < 1 || p.getStock() < n)
                throw new StockInsuficienteException("Stock insuficiente para " + p.getNombre());
        });
        double brutoEntradas = seleccion.stream().mapToDouble(a -> compra.getFuncion().calcularPrecio(a)).sum();
        double descuentoEntradas = estrategiaDescuento.calcularDescuento(brutoEntradas, seleccion.size());
        double total = brutoEntradas - descuentoEntradas;
        List<Entrada> entradasPreparadas = new ArrayList<>();
        for (Asiento a : seleccion) {
            entradasPreparadas.add(new Entrada(UUID.randomUUID().toString(), compra.getFuncion(), a));
        }
        List<DetalleCompra> detallesPreparados = new ArrayList<>();
        productos.forEach((p, n) -> detallesPreparados.add(
                new DetalleCompra(p.getNombre(), n, p.calcularPrecio())));
        combos.forEach((c, n) -> detallesPreparados.add(
                new DetalleCompra(c.getNombre(), n, c.calcularPrecio())));
        total += productos.entrySet().stream().mapToDouble(e -> e.getKey().calcularPrecio() * e.getValue()).sum();
        total += combos.entrySet().stream().mapToDouble(e -> e.getKey().calcularPrecio() * e.getValue()).sum();
        pago.procesarPago(compra.getCliente().getTarjeta(), total);
        entradasPreparadas.forEach(compra::agregarEntrada);
        detallesPreparados.forEach(compra::agregarDetalle);
        seleccion.forEach(Asiento::ocupar);
        consumo.forEach(Producto::descontarStock);
        compra.registrarProductosConsumidos(consumo);
        compra.confirmar(total, descuentoEntradas);
        compra.setMovimientoPuntos(compra.getCliente().getRegistroPuntos().acumularCompra(total));
        compras.add(compra);
        notificar(compra);
        return compra;
    }

    public synchronized double estimarTotal(Funcion funcion, List<Asiento> seleccion,
                                             Map<Producto, Integer> productos,
                                             Map<IComponenteConfiteria, Integer> combos) {
        if (funcion == null || seleccion == null) return 0;
        double subtotalEntradas = seleccion.stream().mapToDouble(funcion::calcularPrecio).sum();
        double total = subtotalEntradas - estrategiaDescuento.calcularDescuento(subtotalEntradas, seleccion.size());
        if (productos != null)
            total += productos.entrySet().stream().mapToDouble(e -> e.getKey().calcularPrecio() * e.getValue()).sum();
        if (combos != null)
            total += combos.entrySet().stream().mapToDouble(e -> e.getKey().calcularPrecio() * e.getValue()).sum();
        return total;
    }

    private void acumularProductos(IComponenteConfiteria c, int cantidad, Map<Producto, Integer> consumo) {
        if (c instanceof Producto p) consumo.merge(p, cantidad, Integer::sum);
        else if (c instanceof Combo combo)
            for (IComponenteConfiteria hijo : combo.getComponentes()) acumularProductos(hijo, cantidad, consumo);
        else if (c instanceof ComboConPromocion decorador)
            acumularProductos(decorador.getComponente(), cantidad, consumo);
    }

    public synchronized List<Compra> consultarCompras() { return List.copyOf(compras); }

    public synchronized List<Compra> comprasDe(Cliente cliente) {
        return compras.stream().filter(c -> c.getCliente() == cliente).toList();
    }

    public synchronized Compra cancelarCompra(String idCompra, Cliente solicitante) {
        Compra compra = compras.stream().filter(c -> c.getId().equals(idCompra)).findFirst()
                .orElseThrow(() -> new CompraInvalidaException("No existe esa compra."));
        if (compra.getCliente() != solicitante) throw new AccesoDenegadoException("Solo puedes cancelar tus propias compras.");
        return aplicarReembolso(compra, politicaReembolso);
    }

    public synchronized Compra cancelarCompraPorCine(String idCompra) {
        Compra compra = compras.stream().filter(c -> c.getId().equals(idCompra)).findFirst()
                .orElseThrow(() -> new CompraInvalidaException("No existe esa compra."));
        return aplicarReembolso(compra, new ReembolsoPorCancelacionCine());
    }

    private Compra aplicarReembolso(Compra compra, IPoliticaReembolso politica) {
        if (!compra.estaConfirmada())
            throw new CompraInvalidaException("Solo puedes cancelar una compra confirmada.");
        double montoReembolso = politica.calcularMonto(
                compra.getTotal(), compra.getFuncion().getFechaHora(), java.time.LocalDateTime.now());
        compra.getEntradas().forEach(e -> e.getAsiento().liberar());
        compra.getProductosConsumidos().forEach(Producto::devolverStock);
        compra.getCliente().getTarjeta().reembolsar(montoReembolso);
        if (compra.getIdMovimientoPuntos()!=null) compra.getCliente().getRegistroPuntos().revertirMovimiento(compra.getIdMovimientoPuntos());
        compra.cancelar();
        notificar(compra);
        return compra;
    }

}


