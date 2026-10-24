package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.CinemaUQ;
import co.edu.uniquindio.poo.model.*;
import java.util.*;

public class CinemaController {
    private final CinemaUQ cine = CinemaUQ.getInstancia();
    private final ServicioCineFacade facade = cine.getServicio();
    private Cliente clienteActivo;
    private final Map<Combo, IComponenteConfiteria> combosConPrecio = new IdentityHashMap<>();
    private Funcion funcion;
    private final Set<Asiento> asientos = new LinkedHashSet<>();
    private final Map<Producto, Integer> productos = new LinkedHashMap<>();
    private final Map<IComponenteConfiteria, Integer> combos = new LinkedHashMap<>();

    public List<Pelicula> peliculas() {
        return facade.cartelera();
    }

    public List<Funcion> funciones(Pelicula pelicula) {
        return facade.funciones(pelicula);
    }

    public List<Funcion> buscarFunciones(FiltroBusqueda filtro, String valor) {
        return facade.buscarFunciones(filtro, valor);
    }

    public Cliente cliente() {
        return clienteActivo;
    }

    public boolean clienteAutenticado() {
        return clienteActivo != null;
    }

    public Cliente iniciarSesionCliente(String correo, String clave) {
        clienteActivo = cine.getGestorUsuario().autenticarCliente(correo, clave);
        return clienteActivo;
    }

    public Cliente crearCuentaCliente(String nombre, String correo, String clave) {
        clienteActivo = cine.getGestorUsuario().registrarCliente(nombre, correo, clave);
        return clienteActivo;
    }

    public boolean autenticarAdministrador(String correo, String clave) {
        return cine.getGestorUsuario().autenticarAdministrador(correo, clave);
    }

    public Administrador administrador() {
        return cine.getGestorUsuario().getAdministrador();
    }

    public void cerrarSesion() {
        clienteActivo = null;
        funcion = null;
        asientos.clear();
        productos.clear();
        combos.clear();
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public void setFuncion(Funcion funcion) {
        if (this.funcion != funcion) asientos.clear();
        this.funcion = funcion;
    }

    public Set<Asiento> getAsientos() {
        return Collections.unmodifiableSet(asientos);
    }

    public void alternarAsiento(Asiento asiento) {
        if (asientos.contains(asiento)) asientos.remove(asiento);
        else if (asiento.estaDisponible()) asientos.add(asiento);
        else throw new AsientoNoDisponibleException("Ese asiento ya está ocupado.");
    }

    public List<Producto> productos() {
        return facade.productos();
    }

    public List<Combo> combos() {
        return facade.combos();
    }

    private IComponenteConfiteria precioCombo(Combo combo) {
        return facade.promocionar(combo).orElse(combo);
    }

    public String textoCombo(Combo combo) {
        return precioCombo(combo).toString();
    }

    public void agregarProducto(Producto producto, int cantidad) {
        if (!clienteAutenticado()) throw new AccesoDenegadoException("Ingresa a tu cuenta para comprar.");
        if (cantidad < 1) throw new CompraInvalidaException("La cantidad debe ser positiva.");
        productos.merge(producto, cantidad, Integer::sum);
    }

    public void agregarCombo(Combo combo, int cantidad) {
        if (!clienteAutenticado()) throw new AccesoDenegadoException("Ingresa a tu cuenta para comprar.");
        if (cantidad < 1) throw new CompraInvalidaException("La cantidad debe ser positiva.");
        combos.merge(precioCombo(combo), cantidad, Integer::sum);
    }

    public double estimarTotal() {
        if (funcion == null) return 0;
        double total = asientos.stream().mapToDouble(a -> funcion.calcularPrecio(a)).sum();
        total += productos.entrySet().stream().mapToDouble(e -> e.getKey().calcularPrecio() * e.getValue()).sum();
        total += combos.entrySet().stream().mapToDouble(e -> e.getKey().calcularPrecio() * e.getValue()).sum();
        return total;
    }

    public String pagar() {
        if (!clienteAutenticado()) throw new AccesoDenegadoException("Ingresa a tu cuenta para comprar.");
        if (funcion == null) throw new CompraInvalidaException("Selecciona una función antes de comprar.");
        double puntosAntes = clienteActivo.getRegistroPuntos().consultarSaldo();
        Compra compra = new Compra(clienteActivo, funcion);
        Compra confirmada = facade.comprar(compra, new ArrayList<>(asientos), productos, combos);
        double saldoRestante = clienteActivo.getTarjeta().getSaldo();
        int puntosSumados = clienteActivo.getRegistroPuntos().consultarSaldo() - (int) puntosAntes;
        asientos.clear();
        productos.clear();
        combos.clear();
        return "Compra " + confirmada.getId().substring(0, 8) + " verificada.\n"
                + (confirmada.getDescuentoEntradas() > 0 ? "Descuento (10% en 5 o más entradas): $" + String.format("%,.0f", confirmada.getDescuentoEntradas()) + " COP\n" : "")
                + "Pagado: $" + String.format("%,.0f", confirmada.getTotal()) + " COP\n"
                + "Saldo restante: $" + String.format("%,.0f", saldoRestante) + " COP\n"
                + "Puntos ganados: " + puntosSumados;
    }

    public String resumenTarjeta() {
        if (!clienteAutenticado()) return "Inicia sesión para consultar tu tarjeta.";
        TarjetaVirtual tarjeta = clienteActivo.getTarjeta();
        return "Tarjeta " + tarjeta.getNumero() + " · Saldo: $" + String.format("%,.0f", tarjeta.getSaldo())
                + " · Puntos: " + clienteActivo.getRegistroPuntos().consultarSaldo();
    }

    public String resumenCarrito() {
        if (!clienteAutenticado()) return "Inicia sesión para armar tu compra.";
        StringBuilder texto = new StringBuilder();
        if (funcion != null)
            texto.append(funcion.getPelicula().getTitulo()).append(" · ").append(funcion.getFechaLegible()).append(System.lineSeparator());
        asientos.forEach(a -> texto.append("Asiento ").append(a.getFila()).append(a.getNumero()).append(" · $")
                .append(String.format("%,.0f", funcion.calcularPrecio(a))).append(System.lineSeparator()));
        productos.forEach((p, n) -> texto.append(n).append(" × ").append(p.getNombre()).append(" · $")
                .append(String.format("%,.0f", p.calcularPrecio() * n)).append(System.lineSeparator()));
        combos.forEach((c, n) -> texto.append(n).append(" × ").append(c.getNombre()).append(" · $")
                .append(String.format("%,.0f", c.calcularPrecio() * n)).append(System.lineSeparator()));
        return texto.length() == 0 ? "Elige una función y agrega asientos o productos." : texto.toString();
    }

    public List<MovimientoTarjeta> movimientos() {
        return clienteAutenticado() ? clienteActivo.getTarjeta().getMovimientos() : List.of();
    }

    public List<Compra> compras() {
        return facade.getGestorCompra().consultarCompras();
    }

    public List<Compra> comprasDelCliente() {
        return clienteAutenticado() ? facade.comprasDe(clienteActivo) : List.of();
    }

    public Compra cancelarCompra(String id) {
        if (!clienteAutenticado()) throw new AccesoDenegadoException("Inicia sesión.");
        return facade.cancelarCompra(id, clienteActivo);
    }

    public SolicitudRecarga solicitarRecarga(double monto) {
        if (!clienteAutenticado()) throw new AccesoDenegadoException("Inicia sesión.");
        return cine.getGestorRecarga().solicitar(clienteActivo, monto);
    }

    public List<SolicitudRecarga> solicitudesRecarga() {
        return cine.getGestorRecarga().listar();
    }

    public SolicitudRecarga aprobarRecarga(String id) {
        return cine.getGestorRecarga().aprobar(id);
    }

    public SolicitudRecarga rechazarRecarga(String id) {
        return cine.getGestorRecarga().rechazar(id);
    }

    public int registrarAsistencia(Compra compra, String idEntrada) {
        return compra.registrarAsistencia(idEntrada);
    }

    public int canjearPuntos(int puntos) {
        if (!clienteAutenticado() || puntos < 100 || puntos % 100 != 0)
            throw new IllegalArgumentException("El canje mínimo es de 100 puntos y se hace en múltiplos de 100.");
        if (!clienteActivo.getRegistroPuntos().canjear(puntos))
            throw new IllegalArgumentException("No tienes suficientes puntos vigentes.");
        double valor = puntos * 10.0;
        clienteActivo.getTarjeta().acreditarCanje(valor);
        return (int) valor;
    }

    public List<Sala> salas() {
        return cine.getGestorSala().listar();
    }

    public Sala registrarSala(String id, int filas, int columnas, TipoSala tipo) {
        return cine.getGestorSala().registrar(id, filas, columnas, tipo);
    }

    public boolean eliminarSala(String id) {
        return cine.getGestorSala().eliminar(id);
    }

    public Compra registrarCompraAdministrador(Cliente cliente, Funcion funcion, List<Asiento> asientosVenta,
                                               List<Producto> productosVenta, List<Combo> combosVenta) {
        if (cliente == null || funcion == null)
            throw new CompraInvalidaException("Selecciona un cliente y una función.");
        Map<Producto, Integer> productosSeleccionados = new LinkedHashMap<>();
        if (productosVenta != null) productosVenta.forEach(p -> productosSeleccionados.merge(p, 1, Integer::sum));
        Map<IComponenteConfiteria, Integer> combosSeleccionados = new LinkedHashMap<>();
        if (combosVenta != null) combosVenta.forEach(c -> combosSeleccionados.merge(precioCombo(c), 1, Integer::sum));
        Compra compra = new Compra(cliente, funcion);
        return facade.comprar(compra, asientosVenta == null ? List.of() : List.copyOf(asientosVenta),
                productosSeleccionados, combosSeleccionados);
    }

    public String generarReporteVentas() {
        return generarReporte(TipoReporte.VENTAS_POR_PERIODO);
    }

    public String generarReporte(TipoReporte tipo) {
        return generarReporte(new SolicitudReporte(tipo, FormatoReporte.TXT, null, null));
    }

    public String generarReporte(SolicitudReporte solicitud) {
        return cine.getGestorReporte().generarReporte(solicitud);
    }

    public void exportarReporte(java.nio.file.Path destino, SolicitudReporte solicitud) throws java.io.IOException {
        String contenido = generarReporte(solicitud);
        java.nio.file.Files.write(destino, contenido.getBytes(solicitud.getFormato() == FormatoReporte.PDF
                ? java.nio.charset.StandardCharsets.US_ASCII : java.nio.charset.StandardCharsets.UTF_8));
    }

    public Pelicula registrarPelicula(String titulo, GeneroPelicula genero, String clasificacion, int duracion,
                                      String sinopsis, String sala, TipoSala tipoSala, java.time.LocalDateTime fechaHora,
                                      double precioBase, int filas, int columnas) {
        Pelicula pelicula = new Pelicula(UUID.randomUUID().toString(), titulo, genero, clasificacion, duracion, sinopsis);
        cine.getGestorSala().registrarSiNoExiste(sala, filas, columnas, tipoSala);
        facade.programarFuncion(new Funcion(UUID.randomUUID().toString(), pelicula, sala, tipoSala, fechaHora, precioBase, filas, columnas));
        return pelicula;
    }

    public Sala clonarSala(String idPlantilla, String nuevoId) {
        return cine.getGestorSala().crearDesdePlantilla(nuevoId, idPlantilla);
    }

    public Funcion programarDesdePlantilla(Funcion plantilla, String idNuevo, String salaNueva,
                                           java.time.LocalDateTime horarioNuevo) {
        Funcion copia = facade.programarDesdePlantilla(plantilla, idNuevo, salaNueva, horarioNuevo);
        cine.getGestorSala().registrar(copia.getSalaModelo());
        return copia;
    }

    public Funcion programarFuncion(Pelicula pelicula, String sala, java.time.LocalDateTime fechaHora,
                                    double precioBase, int filas, int columnas) {
        return programarFuncion(pelicula, sala, TipoSala.DOS_D, fechaHora, precioBase, filas, columnas);
    }

    public Funcion programarFuncion(Pelicula pelicula, String sala, TipoSala tipoSala, java.time.LocalDateTime fechaHora,
                                    double precioBase, int filas, int columnas) {
        cine.getGestorSala().registrarSiNoExiste(sala, filas, columnas, tipoSala);
        Funcion funcionNueva = new Funcion(UUID.randomUUID().toString(), pelicula, sala, tipoSala, fechaHora, precioBase, filas, columnas);
        facade.programarFuncion(funcionNueva);
        return funcionNueva;
    }

    public Producto registrarProducto(String nombre, double precio, int stock) {
        Producto producto = new Producto(UUID.randomUUID().toString(), nombre, precio, stock);
        facade.agregarProducto(producto);
        return producto;
    }

    public Combo registrarCombo(String nombre, List<Producto> componentes) {
        if (componentes == null || componentes.isEmpty())
            throw new IllegalArgumentException("Selecciona uno o más productos para el combo.");
        Combo combo = new Combo(UUID.randomUUID().toString(), nombre);
        componentes.forEach(combo::agregarComponente);
        facade.agregarCombo(combo);
        return combo;
    }

    public Promocion registrarPromocion(String nombre, double descuento, java.time.LocalDate inicio, java.time.LocalDate fin) {
        Promocion promocion = new Promocion(nombre, descuento, inicio, fin);
        facade.agregarPromocion(promocion);
        return promocion;
    }

    public void abastecerProducto(Producto producto, int cantidad) {
        producto.abastecer(cantidad);
    }

    public double recargarTarjeta(Cliente cliente, double monto) {
        if (cliente == null) throw new IllegalArgumentException("Selecciona un cliente.");
        return cine.getGestorTarjeta().recargar(cliente.getTarjeta(), monto);
    }

    public List<Funcion> todasLasFunciones() {
        return facade.todasLasFunciones();
    }

    public List<Promocion> promociones() {
        return facade.promociones();
    }

    public List<Cliente> clientesRegistrados() {
        return cine.getGestorUsuario().listarClientes();
    }
}
