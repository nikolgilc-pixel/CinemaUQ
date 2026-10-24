package co.edu.uniquindio.poo.view;

import co.edu.uniquindio.poo.controller.CinemaController;
import co.edu.uniquindio.poo.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class CinemaView {
    private final CinemaController controller;
    private final Runnable alVolver;
    private final BorderPane root = new BorderPane();
    private final ComboBox<Pelicula> peliculas = new ComboBox<>();
    private final ComboBox<Funcion> funciones = new ComboBox<>();
    private final Label infoPelicula = new Label("Escoge una película y una función para ver los horarios.");
    private final GridPane mapa = new GridPane();
    private final ListView<Producto> listaProductos = new ListView<>();
    private final ListView<Combo> listaCombos = new ListView<>();
    private final TextArea carrito = new TextArea();
    private final TextArea movimientos = new TextArea();
    private final Label estado = new Label("Selecciona tus asientos para empezar.");
    private final Label infoTarjeta = new Label();
    private final Label total = new Label();

    public CinemaView(CinemaController controller, Runnable alVolver) {
        this.controller = controller;
        this.alVolver = alVolver;
        crear();
    }

    public Parent getRoot() {
        return root;
    }

    private void crear() {
        root.getStyleClass().add("app-root");
        root.setTop(crearEncabezado());

        VBox selector = crearSelector();
        VBox asientos = crearSelectorAsientos();
        VBox columnaIzquierda = new VBox(16, selector, asientos);
        columnaIzquierda.getStyleClass().add("column-stack");

        VBox confiteria = crearConfiteria();
        VBox pago = crearPago();
        VBox columnaDerecha = new VBox(16, confiteria, pago);
        columnaDerecha.getStyleClass().add("column-stack");
        columnaDerecha.setPrefWidth(390);
        columnaDerecha.setMinWidth(330);

        HBox contenido = new HBox(20, columnaIzquierda, columnaDerecha);
        contenido.getStyleClass().add("content-layout");
        HBox.setHgrow(columnaIzquierda, Priority.ALWAYS);
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-scroll");
        root.setCenter(scroll);

        peliculas.getItems().setAll(controller.peliculas());
        peliculas.setPromptText("Selecciona película");
        funciones.setPromptText("Selecciona función");
        peliculas.setOnAction(e -> actualizarFunciones());
        if (controller.getFuncion() != null) {
            peliculas.getSelectionModel().select(controller.getFuncion().getPelicula());
            funciones.getItems().setAll(controller.funciones(controller.getFuncion().getPelicula()));
            funciones.getSelectionModel().select(controller.getFuncion());
            actualizarPelicula();
            dibujarAsientos();
        }
        funciones.setOnAction(e -> {
            Funcion funcion = funciones.getValue();
            if (funcion == null) return;
            controller.setFuncion(funcion);
            actualizarPelicula();
            dibujarAsientos();
            actualizarResumen();
        });
        listaProductos.getItems().setAll(controller.productos());
        listaCombos.getItems().setAll(controller.combos());
        listaProductos.setCellFactory(v -> celdaConfiteria());
        listaCombos.setCellFactory(v -> celdaCombo());
        actualizarResumen();
    }

    private HBox crearEncabezado() {
        Label titulo = new Label("CINEMAUQ");
        titulo.getStyleClass().add("brand-title");
        Label subtitulo = new Label("Tu próxima historia empieza aquí");
        subtitulo.getStyleClass().add("header-subtitle");
        VBox marca = new VBox(3, titulo, subtitulo);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Button cartelera = new Button("←  Cartelera");
        cartelera.getStyleClass().add("report-button");
        cartelera.setOnAction(e -> alVolver.run());
        Button perfil = new Button("Mi perfil");
        perfil.getStyleClass().add("secondary-button");
        perfil.setOnAction(e -> abrirPerfil());
        Button salir = new Button("Cerrar sesión");
        salir.getStyleClass().add("secondary-button");
        salir.setOnAction(e -> {
            controller.cerrarSesion();
            alVolver.run();
        });
        HBox header = new HBox(14, marca, espacio, cartelera, perfil, salir);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("app-header");
        return header;
    }

    private VBox crearSelector() {
        Label titulo = new Label("01  ·  ELIGE TU FUNCIÓN");
        titulo.getStyleClass().add("section-title");
        infoPelicula.getStyleClass().add("movie-description");
        VBox panel = new VBox(14, titulo, new HBox(12, campo("PELÍCULA", peliculas), campo("HORARIO", funciones)), infoPelicula);
        panel.getStyleClass().addAll("panel", "movie-panel");
        HBox.setHgrow(peliculas, Priority.ALWAYS);
        HBox.setHgrow(funciones, Priority.ALWAYS);
        return panel;
    }

    private VBox campo(String nombre, Control control) {
        Label etiqueta = new Label(nombre);
        etiqueta.getStyleClass().add("field-label");
        control.getStyleClass().add("dark-input");
        VBox campo = new VBox(6, etiqueta, control);
        HBox.setHgrow(campo, Priority.ALWAYS);
        campo.setMaxWidth(Double.MAX_VALUE);
        return campo;
    }

    private VBox crearSelectorAsientos() {
        Label titulo = new Label("02  ·  SELECCIONA TUS ASIENTOS");
        titulo.getStyleClass().add("section-title");
        Label pantalla = new Label("P A N T A L L A");
        pantalla.getStyleClass().add("screen-label");
        Region barra = new Region();
        barra.getStyleClass().add("screen-bar");
        VBox pantallaBox = new VBox(7, pantalla, barra);
        pantallaBox.setAlignment(Pos.CENTER);
        mapa.setHgap(8);
        mapa.setVgap(8);
        mapa.setAlignment(Pos.CENTER);
        HBox leyenda = new HBox(14, leyenda("#34845f", "General"), leyenda("#8253c7", "VIP"), leyenda("#197c91", "Accesible"), leyenda("#514d57", "Ocupado"), leyenda("#e9b949", "Elegido"));
        leyenda.setAlignment(Pos.CENTER);
        VBox panel = new VBox(17, titulo, pantallaBox, mapa, leyenda);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.getStyleClass().addAll("panel", "seat-panel");
        return panel;
    }

    private HBox leyenda(String color, String texto) {
        Region punto = new Region();
        punto.getStyleClass().add("legend-dot");
        punto.setStyle("-fx-background-color:" + color + ";");
        Label nombre = new Label(texto);
        nombre.getStyleClass().add("legend-text");
        return new HBox(6, punto, nombre);
    }

    private VBox crearConfiteria() {
        Label titulo = new Label("03  ·  ALGO PARA DISFRUTAR");
        titulo.getStyleClass().add("section-title");
        Label etiquetaProductos = new Label("PRODUCTOS");
        etiquetaProductos.getStyleClass().add("field-label");
        Label etiquetaCombos = new Label("COMBOS CON PROMOCIÓN");
        etiquetaCombos.getStyleClass().add("field-label");
        listaProductos.setPrefHeight(125);
        listaCombos.setPrefHeight(105);
        listaProductos.getStyleClass().add("concession-list");
        listaCombos.getStyleClass().add("concession-list");

        Button agregarProducto = new Button("＋  Añadir producto");
        agregarProducto.getStyleClass().add("secondary-button");
        agregarProducto.setMaxWidth(Double.MAX_VALUE);
        agregarProducto.setOnAction(e -> {
            Producto producto = listaProductos.getSelectionModel().getSelectedItem();
            if (producto == null) {
                mostrarEstado("Selecciona un producto de la lista.");
                return;
            }
            controller.agregarProducto(producto, 1);
            actualizarResumen();
            mostrarEstado(producto.getNombre() + " añadido.");
        });

        Button agregarCombo = new Button("＋  Añadir combo");
        agregarCombo.getStyleClass().add("secondary-button");
        agregarCombo.setMaxWidth(Double.MAX_VALUE);
        agregarCombo.setOnAction(e -> {
            Combo combo = listaCombos.getSelectionModel().getSelectedItem();
            if (combo == null) {
                mostrarEstado("Selecciona un combo de la lista.");
                return;
            }
            controller.agregarCombo(combo, 1);
            actualizarResumen();
            mostrarEstado(combo.getNombre() + " añadido.");
        });

        VBox panel = new VBox(9, titulo, etiquetaProductos, listaProductos, agregarProducto, etiquetaCombos, listaCombos, agregarCombo);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private ListCell<Producto> celdaConfiteria() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        };
    }

    private ListCell<Combo> celdaCombo() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Combo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : controller.textoCombo(item));
            }
        };
    }

    private VBox crearPago() {
        Label titulo = new Label("04  ·  TU PEDIDO");
        titulo.getStyleClass().add("section-title");
        carrito.setEditable(false);
        carrito.setWrapText(true);
        carrito.setPrefRowCount(5);
        carrito.getStyleClass().add("summary-area");
        infoTarjeta.getStyleClass().add("wallet-label");
        Label saldoTitulo = new Label("TARJETA VIRTUAL");
        saldoTitulo.getStyleClass().add("field-label");
        total.getStyleClass().add("total-label");
        estado.getStyleClass().add("status-label");
        estado.setWrapText(true);

        Button pagar = new Button("Pagar con mi tarjeta  →");
        pagar.getStyleClass().add("primary-button");
        pagar.setMaxWidth(Double.MAX_VALUE);
        pagar.setOnAction(e -> pagar());
        Label movimientosLabel = new Label("MOVIMIENTOS RECIENTES");
        movimientosLabel.getStyleClass().add("field-label");
        movimientos.setEditable(false);
        movimientos.setPrefRowCount(3);
        movimientos.getStyleClass().add("movement-area");

        VBox panel = new VBox(10, titulo, carrito, saldoTitulo, infoTarjeta, total, pagar, estado, movimientosLabel, movimientos);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private void actualizarFunciones() {
        Pelicula pelicula = peliculas.getValue();
        funciones.getItems().clear();
        if (pelicula == null) return;
        funciones.getItems().setAll(controller.funciones(pelicula));
        if (!funciones.getItems().isEmpty()) funciones.getSelectionModel().selectFirst();
    }

    private void actualizarPelicula() {
        Pelicula pelicula = peliculas.getValue();
        if (pelicula == null) return;
        infoPelicula.setText(pelicula.getGenero() + "   ·   " + pelicula.getDuracionMinutos() + " min   ·   " + pelicula.getClasificacion() + "\n" + pelicula.getSinopsis());
    }

    private void dibujarAsientos() {
        mapa.getChildren().clear();
        int columnas = controller.getFuncion().getSalaModelo().getCapacidad() > 40 ? 10 : 8;
        int indice = 0;
        for (Asiento asiento : controller.getFuncion().getAsientos()) {
            Button boton = new Button(asiento.getFila() + asiento.getNumero());
            boton.getStyleClass().add("seat-button");
            boton.setPrefSize(43, 36);
            pintar(boton, asiento);
            boton.setOnAction(e -> {
                try {
                    controller.alternarAsiento(asiento);
                    pintar(boton, asiento);
                    actualizarResumen();
                    mostrarEstado("Tu selección se actualizó.");
                } catch (RuntimeException ex) {
                    mostrarEstado(ex.getMessage());
                }
            });
            mapa.add(boton, indice % columnas, indice / columnas);
            indice++;
        }
    }

    private void pintar(Button boton, Asiento asiento) {
        boton.getStyleClass().removeAll("seat-general", "seat-vip", "seat-taken", "seat-selected");
        if (!asiento.estaDisponible()) boton.getStyleClass().add("seat-taken");
        else if (controller.getAsientos().contains(asiento)) boton.getStyleClass().add("seat-selected");
        else if (asiento.getTipo() == TipoAsiento.VIP) boton.getStyleClass().add("seat-vip");
        else boton.getStyleClass().add("seat-general");
    }


    private void abrirPerfil() {
        Cliente cliente = controller.cliente();
        Alert opciones = new Alert(Alert.AlertType.NONE);
        opciones.setTitle("Mi perfil · CINEMAX");
        opciones.setHeaderText(cliente.getNombre() + " · " + cliente.getEmail());
        String movimientosPerfil = cliente.getTarjeta().getMovimientos().stream().skip(Math.max(0, cliente.getTarjeta().getMovimientos().size() - 5)).map(m -> m.tipo() + " $" + String.format("%,.0f", m.monto())).collect(java.util.stream.Collectors.joining("\n"));
        opciones.setContentText(controller.resumenTarjeta() + "\nPuntos vigentes: " + cliente.getRegistroPuntos().consultarSaldo() + "\nÚltimos movimientos:\n" + (movimientosPerfil.isBlank() ? "Sin movimientos" : movimientosPerfil));
        ButtonType recargar = new ButtonType("Solicitar recarga");
        ButtonType compras = new ButtonType("Mis compras / cancelar");
        ButtonType canje = new ButtonType("Canjear puntos");
        ButtonType asistencia = new ButtonType("Registrar asistencia");
        ButtonType cerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        opciones.getButtonTypes().setAll(recargar, compras, canje, asistencia, cerrar);
        opciones.showAndWait().ifPresent(seleccion -> {
            if (seleccion == recargar) {
                TextInputDialog monto = new TextInputDialog();
                monto.setTitle("Solicitar recarga");
                monto.setHeaderText("La recarga se acredita cuando la apruebe administración.");
                monto.setContentText("Monto en COP:");
                monto.showAndWait().ifPresent(valor -> {
                    try {
                        SolicitudRecarga s = controller.solicitarRecarga(Double.parseDouble(valor));
                        mostrarEstado("Solicitud enviada por $" + String.format("%,.0f", s.getMonto()) + ". Está pendiente de aprobación.");
                    } catch (RuntimeException ex) {
                        mostrarEstado("Monto inválido. Escribe un valor numérico positivo.");
                    }
                });
            } else if (seleccion == canje) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Canje de puntos");
                dialog.setHeaderText("Cada 100 puntos acredita $1.000 COP en tu tarjeta.");
                dialog.setContentText("Puntos a canjear:");
                dialog.showAndWait().ifPresent(valor -> {
                    try {
                        int acreditado = controller.canjearPuntos(Integer.parseInt(valor));
                        mostrarEstado("Canje aprobado: se acreditaron $" + String.format("%,.0f", acreditado) + " COP en tu tarjeta.");
                        actualizarResumen();
                    } catch (RuntimeException ex) {
                        mostrarEstado(ex.getMessage());
                    }
                });
            } else if (seleccion == asistencia) {
                List<Compra> historial = controller.comprasDelCliente().stream().filter(c -> !c.isCancelada() && c.getEntradas().stream().anyMatch(en -> !en.isAsistida())).toList();
                if (historial.isEmpty()) {
                    mostrarEstado("No hay entradas pendientes de validar.");
                    return;
                }
                ChoiceDialog<Compra> cd = new ChoiceDialog<>(historial.get(0), historial);
                cd.setTitle("Validar asistencia");
                cd.setHeaderText("La asistencia se valida después de finalizar la función.");
                cd.setContentText("Compra:");
                cd.showAndWait().ifPresent(compra -> {
                    List<Entrada> pendientes = compra.getEntradas().stream().filter(en -> !en.isAsistida()).toList();
                    ChoiceDialog<Entrada> ed = new ChoiceDialog<>(pendientes.get(0), pendientes);
                    ed.setTitle("Entrada");
                    ed.setHeaderText("Selecciona la entrada usada.");
                    ed.showAndWait().ifPresent(entrada -> {
                        try {
                            controller.registrarAsistencia(compra, entrada.getId());
                            mostrarEstado("Asistencia registrada: ganaste 20 puntos.");
                            actualizarResumen();
                        } catch (RuntimeException ex) {
                            mostrarEstado(ex.getMessage());
                        }
                    });
                });
            } else if (seleccion == compras) {
                List<Compra> historial = controller.comprasDelCliente().stream().filter(c -> !c.isCancelada()).toList();
                if (historial.isEmpty()) {
                    mostrarEstado("Aún no tienes compras activas para cancelar.");
                    return;
                }
                ChoiceDialog<Compra> dialog = new ChoiceDialog<>(historial.get(0), historial);
                dialog.setTitle("Mis compras");
                dialog.setHeaderText("Selecciona una compra. Se puede cancelar hasta 2 horas antes de la función.");
                dialog.setContentText("Compra:");
                dialog.showAndWait().ifPresent(compra -> {
                    try {
                        controller.cancelarCompra(compra.getId());
                        mostrarEstado("Compra cancelada; el reembolso quedó en tu tarjeta y los asientos volvieron a estar disponibles.");
                        dibujarAsientos();
                        actualizarResumen();
                    } catch (RuntimeException ex) {
                        mostrarEstado(ex.getMessage());
                    }
                });
            }
        });
    }

    private void pagar() {
        try {
            String confirmacion = controller.pagar();
            dibujarAsientos();
            actualizarResumen();
            mostrarEstado("¡Compra verificada! El saldo y los puntos ya fueron actualizados.");
            Alert recibo = new Alert(Alert.AlertType.INFORMATION, confirmacion, ButtonType.OK);
            recibo.setTitle("CINEMAX · Compra confirmada");
            recibo.setHeaderText("¡Tu compra está lista!");
            recibo.showAndWait();
        } catch (RuntimeException ex) {
            mostrarEstado(ex.getMessage());
        }
    }

    private void actualizarResumen() {
        carrito.setText(controller.resumenCarrito());
        infoTarjeta.setText(controller.resumenTarjeta());
        total.setText(String.format("Total estimado   $%,.0f COP", controller.estimarTotal()));
        StringBuilder texto = new StringBuilder();
        List<MovimientoTarjeta> recientes = controller.movimientos();
        recientes.stream().skip(Math.max(0, recientes.size() - 3)).forEach(m -> texto.append(m.tipo()).append("  ·  $").append(String.format("%,.0f", m.monto())).append("  ·  ").append(m.fechaHora()).append('\n'));
        if (texto.isEmpty()) texto.append("Todavía no hay movimientos.");
        movimientos.setText(texto.toString());
    }

    private void mostrarEstado(String mensaje) {
        estado.setText(mensaje);
        actualizarResumen();
    }
}






