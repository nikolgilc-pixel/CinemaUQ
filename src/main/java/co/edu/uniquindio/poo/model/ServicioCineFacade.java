package co.edu.uniquindio.poo.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServicioCineFacade {
    private final GestorFuncion gestorFuncion;
    private final GestorPelicula gestorPelicula;
    private final IBusqueda busqueda;
    private final GestorCompra gestorCompra;
    private final GestorProducto gestorProducto;
    private final GestorCombos gestorCombos;
    private final GestorPromocion gestorPromocion;

    public ServicioCineFacade(GestorFuncion funciones, GestorCompra compras, GestorProducto productos,
                              GestorCombos combos, GestorPromocion promociones) {
        this(funciones, new GestorPelicula(funciones.listarPeliculas()), compras, productos, combos, promociones);
    }

    public ServicioCineFacade(GestorFuncion funciones, GestorPelicula peliculas, GestorCompra compras,
                              GestorProducto productos, GestorCombos combos, GestorPromocion promociones) {
        this.gestorFuncion = funciones;
        this.gestorPelicula = peliculas;
        this.busqueda = new BuscarFuncion(funciones);
        this.gestorCompra = compras;
        this.gestorProducto = productos;
        this.gestorCombos = combos;
        this.gestorPromocion = promociones;
    }

    public List<Pelicula> cartelera() {
        return gestorPelicula.listar();
    }

    public List<Funcion> buscarFunciones(FiltroBusqueda filtro, String valor) {
        return busqueda.buscarCriterio(filtro, valor);
    }

    public List<Funcion> funciones(Pelicula pelicula) {
        return gestorFuncion.funcionesDe(pelicula);
    }

    public List<Funcion> todasLasFunciones() {
        return gestorFuncion.listarFunciones().stream().filter(f -> f.getFechaHora().isAfter(java.time.LocalDateTime.now())).toList();
    }

    public List<Producto> productos() {
        return gestorProducto.consultarProductos();
    }

    public List<Combo> combos() {
        return gestorCombos.consultarCombos();
    }

    public List<Promocion> promociones() {
        return gestorPromocion.consultarPromociones();
    }

    public Optional<IComponenteConfiteria> promocionar(IComponenteConfiteria componente) {
        return gestorPromocion.aplicarPromocion(componente);
    }

    public Compra comprar(Compra compra, List<Asiento> asientos, Map<Producto, Integer> productos, Map<IComponenteConfiteria, Integer> combos) {
        return gestorCompra.registrarCompra(compra, asientos, productos, combos);
    }

    public Compra cancelarCompra(String id, Cliente cliente) { return gestorCompra.cancelarCompra(id, cliente); }
    public List<Compra> comprasDe(Cliente cliente) { return gestorCompra.comprasDe(cliente); }

    public Pelicula agregarPelicula(Pelicula pelicula) {
        return gestorPelicula.registrar(pelicula);
    }

    public void programarFuncion(Funcion funcion) {
        gestorPelicula.registrarSiNoExiste(funcion.getPelicula());
        gestorFuncion.programarFuncion(funcion);
    }

    public Funcion programarDesdePlantilla(Funcion plantilla, String nuevoId, String nuevaSala,
                                           java.time.LocalDateTime nuevoHorario) {
        Funcion copia = gestorFuncion.programarDesdePlantilla(plantilla, nuevoId, nuevaSala, nuevoHorario);
        gestorPelicula.registrarSiNoExiste(copia.getPelicula());
        return copia;
    }
    public void agregarProducto(Producto producto) {
        gestorProducto.agregarProducto(producto);
    }

    public void agregarCombo(Combo combo) {
        gestorCombos.agregarCombo(combo);
    }

    public void agregarPromocion(Promocion promocion) {
        gestorPromocion.agregarPromocion(promocion);
    }

    public GestorFuncion getGestorFuncion() { return gestorFuncion; }

    public GestorCompra getGestorCompra() {
        return gestorCompra;
    }
}
