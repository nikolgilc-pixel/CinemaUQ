package co.edu.uniquindio.poo.view;

import co.edu.uniquindio.poo.controller.CinemaController;
import co.edu.uniquindio.poo.model.Funcion;
import co.edu.uniquindio.poo.model.FiltroBusqueda;
import co.edu.uniquindio.poo.model.Pelicula;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.function.Consumer;

public class CarteleraView {
    private final CinemaController controller;
    private final Consumer<Funcion> alComprar;
    private final Runnable alIngresar;
    private final BorderPane root = new BorderPane();
    private final TextField buscador = new TextField();
    private final ComboBox<String> filtroGenero = new ComboBox<>();
    private final ComboBox<String> filtroTipoSala = new ComboBox<>();
    private final DatePicker filtroFecha = new DatePicker();
    private final FlowPane tarjetas = new FlowPane(18, 18);
    private final Label estado = new Label();

    public CarteleraView(CinemaController controller, Consumer<Funcion> alComprar, Runnable alIngresar) {
        this.controller = controller;
        this.alComprar = alComprar;
        this.alIngresar = alIngresar;
        crear();
    }

    public Parent getRoot() {
        return root;
    }

    private void crear() {
        root.getStyleClass().add("app-root");

        Label marca = new Label("CINEMAX");
        marca.getStyleClass().add("brand-title");
        Label subtitulo = new Label("Vive cada historia en pantalla grande");
        subtitulo.getStyleClass().add("header-subtitle");
        VBox marcaBox = new VBox(3, marca, subtitulo);
        marcaBox.setAlignment(Pos.CENTER);
        BorderPane header = new BorderPane();
        header.setCenter(marcaBox);
        Button cuenta = new Button(controller.clienteAutenticado()
                ? "Hola, " + controller.cliente().getNombre() : "Ingresar");
        cuenta.getStyleClass().add("secondary-button");
        cuenta.setOnAction(e -> alIngresar.run());
        header.setRight(cuenta);
        BorderPane.setAlignment(cuenta, Pos.CENTER_RIGHT);
        header.getStyleClass().add("app-header");
        root.setTop(header);

        Label titulo = new Label("ESTRENOS Y CARTELERA");
        titulo.getStyleClass().add("hero-title");
        Label texto = new Label("Busca tu película, elige una función y reserva tus asientos.");
        texto.getStyleClass().add("hero-subtitle");
        buscador.setPromptText("Buscar por película, género o descripción...");
        buscador.getStyleClass().add("dark-input");
        filtroGenero.getStyleClass().add("dark-input");
        filtroGenero.setPromptText("Todos los géneros");
        filtroGenero.getItems().setAll("Todos los géneros");
        controller.peliculas().stream().map(Pelicula::getGenero).distinct().sorted().forEach(filtroGenero.getItems()::add);
        filtroGenero.getSelectionModel().selectFirst();
        filtroTipoSala.getStyleClass().add("dark-input");
        filtroTipoSala.setPromptText("Todos los tipos de sala");
        filtroTipoSala.getItems().setAll("Todos los tipos de sala", "2D", "3D", "4D", "IMAX");
        filtroTipoSala.getSelectionModel().selectFirst();
        filtroFecha.setPromptText("Cualquier fecha");
        filtroFecha.getStyleClass().add("dark-input");
        filtroTipoSala.setOnAction(e -> mostrarPeliculas());
        filtroFecha.setOnAction(e -> mostrarPeliculas());
        buscador.textProperty().addListener((o, antes, ahora) -> mostrarPeliculas());
        filtroGenero.setOnAction(e -> mostrarPeliculas());

        HBox barraBusqueda = new HBox(12, buscador, filtroGenero, filtroTipoSala, filtroFecha);
        barraBusqueda.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(buscador, Priority.ALWAYS);
        buscador.setMaxWidth(Double.MAX_VALUE);
        VBox bienvenida = new VBox(9, titulo, texto, barraBusqueda);
        bienvenida.getStyleClass().add("public-hero");
        Label seccion = new Label("EN CARTELERA  ·  ELIGE TU PRÓXIMA HISTORIA");
        seccion.getStyleClass().add("section-title");
        tarjetas.setPrefWrapLength(1050);
        tarjetas.setMaxWidth(Double.MAX_VALUE);
        tarjetas.setPadding(new Insets(4, 0, 4, 0));
        estado.getStyleClass().add("status-label");
        VBox contenido = new VBox(18, bienvenida, seccion, tarjetas, estado);
        contenido.getStyleClass().add("public-content");
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-scroll");
        root.setCenter(scroll);
        mostrarPeliculas();
    }

    private void mostrarPeliculas() {
        tarjetas.getChildren().clear();
        String filtro = buscador.getText() == null ? "" : buscador.getText().trim();
        String genero = filtroGenero.getValue();
        String tipoSala = filtroTipoSala.getValue();
        LocalDate fecha = filtroFecha.getValue();
        List<Funcion> funcionesEncontradas = filtro.isBlank()
                ? controller.todasLasFunciones()
                : controller.buscarFunciones(FiltroBusqueda.POR_NOMBRE_PELICULA, filtro);
        if (genero != null && !genero.equals("Todos los géneros")) {
            Set<String> porGenero = controller.buscarFunciones(FiltroBusqueda.POR_GENERO, genero)
                    .stream().map(Funcion::getId).collect(java.util.stream.Collectors.toSet());
            funcionesEncontradas = funcionesEncontradas.stream().filter(f -> porGenero.contains(f.getId())).toList();
        }
        if (fecha != null) {
            Set<String> porFecha = controller.buscarFunciones(FiltroBusqueda.POR_FECHA, fecha.toString())
                    .stream().map(Funcion::getId).collect(java.util.stream.Collectors.toSet());
            funcionesEncontradas = funcionesEncontradas.stream().filter(f -> porFecha.contains(f.getId())).toList();
        }
        if (tipoSala != null && !tipoSala.equals("Todos los tipos de sala")) {
            funcionesEncontradas = funcionesEncontradas.stream()
                    .filter(f -> f.getSalaModelo().getTipoSala().toString().equals(tipoSala)).toList();
        }
        List<Pelicula> encontrados = funcionesEncontradas.stream().map(Funcion::getPelicula).distinct().toList();
        for (int i = 0; i < encontrados.size(); i++) tarjetas.getChildren().add(crearTarjeta(encontrados.get(i), i));
        if (encontrados.isEmpty()) {
            Label vacio = new Label("No encontramos películas con esos filtros.");
            vacio.getStyleClass().add("movie-description");
            tarjetas.getChildren().add(vacio);
        }
    }

    private Image cargarImagen(Pelicula pelicula) {
        String ruta = pelicula.getRutaImagen();
        if (ruta != null && ruta.startsWith("classpath:")) {
            URL recurso = getClass().getResource(ruta.substring("classpath:".length()));
            if (recurso != null) return new Image(recurso.toExternalForm(), true);
        } else if (ruta != null) {
            try {
                Path archivo = Path.of(ruta);
                if (Files.isRegularFile(archivo)) return new Image(archivo.toUri().toString(), true);
            } catch (RuntimeException ignored) {
                // Si el archivo ya no existe, se usa el póster predeterminado.
            }
        }
        String archivoImagen = switch (pelicula.getId()) {
            case "P1" -> "poster-anime.jpg";
            case "P2" -> "poster-robot.jpg";
            case "P3" -> "poster-aventura.jpg";
            case "P4" -> "poster-carrera.jpg";
            default -> "poster-kart.jpg";
        };
        URL recurso = getClass().getResource("/imagenes/" + archivoImagen);
        return recurso == null ? null : new Image(recurso.toExternalForm(), true);
    }

    private VBox crearTarjeta(Pelicula pelicula, int indice) {
        Image imagenPortada = cargarImagen(pelicula);
        StackPane poster = new StackPane();
        poster.getStyleClass().add("movie-poster");
        poster.setMinHeight(190);
        poster.setPrefHeight(190);
        if (imagenPortada != null) {
            ImageView imagen = new ImageView(imagenPortada);
            imagen.setFitWidth(280);
            imagen.setFitHeight(190);
            imagen.setPreserveRatio(true);
            imagen.setSmooth(true);
            poster.getChildren().add(imagen);
        }
        VBox datosPoster = new VBox(6);
        datosPoster.setAlignment(Pos.BOTTOM_LEFT);
        datosPoster.getStyleClass().add("poster-overlay");
        Label adorno = new Label("CINEMAX  ·  EN CARTELERA");
        adorno.getStyleClass().add("poster-kicker");
        Label titulo = new Label(pelicula.getTitulo());
        titulo.setWrapText(true);
        titulo.getStyleClass().add("poster-title");
        Label genero = new Label(pelicula.getGenero() + "  ·  " + pelicula.getDuracionMinutos()
                + " min  ·  " + pelicula.getClasificacion());
        genero.getStyleClass().add("poster-meta");
        datosPoster.getChildren().addAll(adorno, titulo, genero);
        poster.getChildren().add(datosPoster);
        StackPane.setAlignment(datosPoster, Pos.BOTTOM_LEFT);

        Label sinopsis = new Label(pelicula.getSinopsis());
        sinopsis.setWrapText(true);
        sinopsis.getStyleClass().add("movie-description");

        ComboBox<Funcion> horarios = new ComboBox<>();
        horarios.getStyleClass().add("dark-input");
        horarios.setMaxWidth(Double.MAX_VALUE);
        horarios.setPromptText("Selecciona horario");
        horarios.getItems().setAll(controller.funciones(pelicula));
        if (!horarios.getItems().isEmpty()) horarios.getSelectionModel().selectFirst();

        Button comprar = new Button("Comprar entradas  →");
        comprar.getStyleClass().add("primary-button");
        comprar.setMaxWidth(Double.MAX_VALUE);
        comprar.setDisable(horarios.getItems().isEmpty());
        comprar.setOnAction(e -> {
            Funcion funcion = horarios.getValue();
            if (funcion != null) alComprar.accept(funcion);
        });

        VBox tarjeta = new VBox(13, poster, sinopsis, horarios, comprar);
        tarjeta.setPrefWidth(310);
        tarjeta.setMaxWidth(340);
        tarjeta.getStyleClass().add("movie-card");
        return tarjeta;
    }
}




