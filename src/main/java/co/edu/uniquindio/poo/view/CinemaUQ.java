package co.edu.uniquindio.poo.view;

import co.edu.uniquindio.poo.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CinemaUQ {
    private static final CinemaUQ INSTANCIA = new CinemaUQ();
    private final Cliente clienteDemo;
    private final ServicioCineFacade servicio;
    private final GestorUsuario gestorUsuario;
    private final GestorRecarga gestorRecarga = new GestorRecarga();
    private final GestorSala gestorSala = new GestorSala();
    private final GestorTarjeta gestorTarjeta = new GestorTarjeta();
    private final GestorPelicula gestorPelicula;
    private final GestorReporte gestorReporte;

    private CinemaUQ() {
        clienteDemo = new Cliente("Nikol Gil", "cliente@cinemauq.edu.co", new TarjetaVirtual("UQ-2048", 120000));
        List<Funcion> funciones = new ArrayList<>();
        Pelicula p1 = new Pelicula("P1", "El viaje de Chihiro", GeneroPelicula.ANIMACION, "7+", 125, "Una aventura fantástica.");
        Pelicula p2 = new Pelicula("P2", "Interestelar", GeneroPelicula.CIENCIA_FICCION, "12+", 169, "Una misión más allá de las estrellas.");
        Pelicula p3 = new Pelicula("P3", "Coco", GeneroPelicula.ANIMACION, "7+", 105, "Una historia sobre familia y música.");
        Pelicula p4 = new Pelicula("P4", "Reto a toda velocidad", GeneroPelicula.AVENTURA, "7+", 112, "Una carrera llena de curvas, amistad y sorpresas.");
        Pelicula p5 = new Pelicula("P5", "Aventura sobre ruedas", GeneroPelicula.FAMILIAR, "7+", 98, "Una divertida aventura para toda la familia.");
        Pelicula p6 = new Pelicula("P6", "The Avengers", GeneroPelicula.SUPERHEROES, "12+", 143, "Los Vengadores se unen para proteger la Tierra de una amenaza inesperada.");
        p6.setRutaImagen("classpath:/imagenes/poster-vengadores.jpg");
        LocalDateTime hoy = LocalDate.now().atTime(19, 0);
        funciones.add(new Funcion("F1", p1, "Sala 1", hoy.plusDays(1), 18000, 5, 8));
        funciones.add(new Funcion("F2", p2, "Sala 2", hoy.plusDays(1).plusHours(2), 18000, 5, 8));
        funciones.add(new Funcion("F3", p3, "Sala 3", hoy.plusDays(2), 16000, 5, 8));
        funciones.add(new Funcion("F4", p4, "Sala 1", hoy.plusDays(2).plusHours(2), 17000, 5, 8));
        funciones.add(new Funcion("F5", p5, "Sala 2", hoy.plusDays(3), 16000, 5, 8));
        funciones.add(new Funcion("F6", p6, "Sala 3", hoy.plusDays(4), 18000, 5, 8));
        gestorSala.registrarSiNoExiste("Sala 1", 5, 8, TipoSala.DOS_D);
        gestorSala.registrarSiNoExiste("Sala 2", 5, 8, TipoSala.DOS_D);
        gestorSala.registrarSiNoExiste("Sala 3", 5, 8, TipoSala.DOS_D);
        List<Producto> productos = new ArrayList<>(List.of(new Producto("P-C1", "Crispetas grandes", 12000, 30), new Producto("P-B1", "Gaseosa", 7000, 40), new Producto("P-D1", "Perro caliente", 10000, 20), new Producto("P-C2", "Crispetas medianas", 9000, 25)));
        Combo combo = new Combo("C1", "Combo pareja");
        combo.agregarComponente(productos.get(0));
        combo.agregarComponente(productos.get(1));
        combo.agregarComponente(productos.get(1));
        Promocion promo = new Promocion("Promo estreno", 10, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        gestorPelicula = new GestorPelicula(List.of(p1, p2, p3, p4, p5, p6));
        gestorTarjeta.registrar(clienteDemo.getTarjeta());
        gestorUsuario = new GestorUsuario(clienteDemo, gestorTarjeta);
        servicio = new ServicioCineFacade(new GestorFuncion(funciones), gestorPelicula,
                new GestorCompra(new ProxyPagoTarjeta(new PagoTarjetaVirtual())), new GestorProducto(productos),
                new GestorCombos(List.of(combo)), new GestorPromocion(promo));
        gestorReporte = new GestorReporte(servicio.getGestorCompra());
    }

    public static CinemaUQ getInstancia() {
        return INSTANCIA;
    }

    public Cliente getClienteDemo() {
        return clienteDemo;
    }

    public ServicioCineFacade getServicio() {
        return servicio;
    }

    public GestorUsuario getGestorUsuario() {
        return gestorUsuario;
    }

    public GestorRecarga getGestorRecarga() {
        return gestorRecarga;
    }

    public GestorSala getGestorSala() {
        return gestorSala;
    }

    public GestorTarjeta getGestorTarjeta() {
        return gestorTarjeta;
    }

    public GestorPelicula getGestorPelicula() {
        return gestorPelicula;
    }

    public GestorReporte getGestorReporte() {
        return gestorReporte;
    }
}
