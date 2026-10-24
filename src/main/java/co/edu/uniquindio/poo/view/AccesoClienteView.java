package co.edu.uniquindio.poo.view;

import co.edu.uniquindio.poo.controller.CinemaController;
import co.edu.uniquindio.poo.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Consumer;

public class AccesoClienteView {
    private final CinemaController controller;
    private final Runnable volver;
    private final Consumer<Cliente> alIngresar;
    private final BorderPane root = new BorderPane();
    private final Label mensaje = new Label();
    private final VBox panel = new VBox(14);
    private TipoReporte tipoReporteSeleccionado = TipoReporte.VENTAS_POR_PERIODO;
    private FormatoReporte formatoReporteSeleccionado = FormatoReporte.TXT;

    public AccesoClienteView(CinemaController controller, Runnable volver, Consumer<Cliente> alIngresar) {
        this.controller = controller;
        this.volver = volver;
        this.alIngresar = alIngresar;
        crear();
    }

    public Parent getRoot() {
        return root;
    }

    private void crear() {
        root.getStyleClass().add("app-root");
        Button volverBtn = new Button("←  Volver a cartelera");
        volverBtn.getStyleClass().add("report-button");
        volverBtn.setOnAction(e -> volver.run());
        root.setTop(volverBtn);
        BorderPane.setAlignment(volverBtn, Pos.CENTER_LEFT);
        BorderPane.setMargin(volverBtn, new Insets(20, 28, 0, 28));
        Label titulo = new Label("CINEMAX");
        titulo.getStyleClass().add("brand-title");
        Label bajada = new Label("Ingresa para continuar con tu experiencia");
        bajada.getStyleClass().add("movie-description");
        panel.getStyleClass().addAll("panel", "auth-panel");
        panel.setMaxWidth(1120);
        panel.setAlignment(Pos.CENTER_LEFT);
        mensaje.getStyleClass().add("status-label");
        mensaje.setWrapText(true);
        mostrarIngreso();
        VBox contenido = new VBox(18, titulo, bajada, panel, mensaje);
        contenido.setAlignment(Pos.CENTER);
        contenido.getStyleClass().add("auth-background");
        root.setCenter(contenido);
    }

    private void mostrarIngreso() {
        Label titulo = new Label("Bienvenido");
        titulo.getStyleClass().add("hero-title");
        Label ayuda = new Label("Ingresa con tu cuenta para comprar. El sistema reconoce automáticamente las credenciales de administración.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("movie-description");
        TextField usuario = new TextField();
        usuario.setPromptText("Correo o usuario");
        PasswordField clave = new PasswordField();
        clave.setPromptText("Contraseña");
        Button ingresar = new Button("Ingresar y continuar");
        ingresar.getStyleClass().add("primary-button");
        ingresar.setMaxWidth(Double.MAX_VALUE);
        ingresar.setOnAction(e -> {
            try {
                if (controller.autenticarAdministrador(usuario.getText(), clave.getText())) crearPanelAdmin();
                else alIngresar.accept(controller.iniciarSesionCliente(usuario.getText(), clave.getText()));
            } catch (RuntimeException ex) {
                mensaje.setText(ex.getMessage());
            }
        });
        Button crear = new Button("Crear cuenta");
        crear.getStyleClass().add("report-button");
        crear.setMaxWidth(Double.MAX_VALUE);
        crear.setOnAction(e -> mostrarRegistro());
        Label demo = new Label("Demo cliente: cliente@cinemauq.edu.co / Cliente123! · Admin: admin / Admin123!");
        demo.setWrapText(true);
        demo.getStyleClass().add("field-label");
        panel.getChildren().setAll(titulo, ayuda, usuario, clave, ingresar, crear, demo);
    }

    private void mostrarRegistro() {
        Label titulo = new Label("Crear cuenta de cliente");
        titulo.getStyleClass().add("hero-title");
        TextField nombre = new TextField();
        nombre.setPromptText("Nombre completo");
        TextField correo = new TextField();
        correo.setPromptText("Correo electrónico");
        PasswordField clave = new PasswordField();
        clave.setPromptText("Contraseña (mínimo 6 caracteres)");
        Button crear = new Button("Crear cuenta y continuar");
        crear.getStyleClass().add("primary-button");
        crear.setMaxWidth(Double.MAX_VALUE);
        crear.setOnAction(e -> {
            try {
                Cliente cliente = controller.crearCuentaCliente(nombre.getText(), correo.getText(), clave.getText());
                mensaje.setText("Cuenta creada. Tu tarjeta demo inicia con $50.000 COP.");
                alIngresar.accept(cliente);
            } catch (RuntimeException ex) {
                mensaje.setText(ex.getMessage());
            }
        });
        Button volverLogin = new Button("Ya tengo cuenta · Ingresar");
        volverLogin.getStyleClass().add("report-button");
        volverLogin.setOnAction(e -> mostrarIngreso());
        panel.getChildren().setAll(titulo, nombre, correo, clave, crear, volverLogin);
    }

    private void crearPanelAdmin() {
        Label titulo = new Label("Administración · " + controller.administrador().getNombre());
        titulo.getStyleClass().add("hero-title");
        Label resumen = new Label("Gestiona películas, funciones, confitería y promociones; registra ventas y consulta la actividad del cine.");
        resumen.setWrapText(true);
        resumen.getStyleClass().add("movie-description");
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPrefHeight(590);
        tabs.getTabs().add(tab("Películas y funciones", crearGestionPeliculas()));
        tabs.getTabs().add(tab("Productos", crearGestionProductos()));
        tabs.getTabs().add(tab("Combos", crearGestionCombos()));
        tabs.getTabs().add(tab("Promociones", crearGestionPromociones()));
        tabs.getTabs().add(tab("Registrar compra", crearRegistroVenta()));
        tabs.getTabs().add(tab("Compras y reportes", crearGestionCompras()));
        tabs.getTabs().add(tab("Clientes", crearGestionClientes()));
        tabs.getTabs().add(tab("Solicitudes de recarga", crearGestionSolicitudesRecarga()));
        tabs.getTabs().add(tab("Salas", crearGestionSalas()));
        tabs.getTabs().add(tab("Recargar tarjeta", crearRecargaTarjeta()));
        Button salir = new Button("Volver a cartelera");
        salir.getStyleClass().add("report-button");
        salir.setOnAction(e -> volver.run());
        panel.setMaxWidth(1160);
        panel.setMaxHeight(800);
        panel.getChildren().setAll(titulo, resumen, tabs, salir);
    }

    private Tab tab(String titulo, VBox contenido) {
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-scroll");
        Tab tab = new Tab(titulo, scroll);
        tab.setClosable(false);
        return tab;
    }

    private VBox form() {
        VBox box = new VBox(11);
        box.setPadding(new Insets(18));
        box.getStyleClass().add("admin-form");
        return box;
    }

    private Label subtitulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("section-title");
        return label;
    }

    private VBox crearGestionPeliculas() {
        VBox box = form();
        box.getChildren().add(subtitulo("Registrar película y su primera función"));
        TextField titulo = campo("Título de la película"), clasificacion = campo("Clasificación, por ejemplo 7+"), duracion = new TextField("100");
        ComboBox<GeneroPelicula> genero = new ComboBox<>();
        genero.getItems().setAll(GeneroPelicula.values());
        genero.setPromptText("Selecciona género");
        genero.setMaxWidth(Double.MAX_VALUE);
        duracion.setPromptText("Duración en minutos");
        TextArea sinopsis = new TextArea();
        sinopsis.setPromptText("Sinopsis");
        sinopsis.setPrefRowCount(2);
        TextField sala = new TextField("Sala 1"), hora = new TextField("19:00"), precio = new TextField("18000"), filas = new TextField("5"), columnas = new TextField("8");
        ComboBox<TipoSala> tipoSala = new ComboBox<>();
        tipoSala.getItems().setAll(TipoSala.values());
        tipoSala.setValue(TipoSala.DOS_D);
        tipoSala.setMaxWidth(Double.MAX_VALUE);
        DatePicker fecha = new DatePicker(LocalDate.now().plusDays(1));
        Button registrar = boton("Registrar película y función", "primary-button");
        registrar.setOnAction(e -> ejecutar(() -> {
            if (genero.getValue() == null) throw new IllegalArgumentException("Selecciona el género.");
            controller.registrarPelicula(obligatorio(titulo, "Escribe el título."), genero.getValue(),
                    obligatorio(clasificacion, "Escribe la clasificación."), entero(duracion, "Duración no válida."), obligatorio(sinopsis, "Escribe la sinopsis."),
                    obligatorio(sala, "Escribe la sala."), tipoSala.getValue(), fechaHora(fecha, hora), numero(precio, "Precio no válido."),
                    entero(filas, "Filas no válidas."), entero(columnas, "Columnas no válidas."));
            titulo.clear();
            genero.setValue(null);
            clasificacion.clear();
            sinopsis.clear();
            mensaje.setText("Película y primera función registradas.");
        }));
        box.getChildren().addAll(titulo, genero, clasificacion, duracion, sinopsis, subtitulo("Primera función y sala"), sala, tipoSala, fecha, hora, precio,
                subtitulo("Capacidad de la sala"), filas, columnas, registrar, new Separator(), subtitulo("Programar otra función para una película existente"));
        ComboBox<Pelicula> peliculas = new ComboBox<>();
        peliculas.setPromptText("Selecciona película");
        peliculas.getItems().setAll(controller.peliculas());
        peliculas.setMaxWidth(Double.MAX_VALUE);
        TextField sala2 = new TextField("Sala 1"), hora2 = new TextField("21:00"), precio2 = new TextField("18000"), filas2 = new TextField("5"), columnas2 = new TextField("8");
        ComboBox<TipoSala> tipoSala2 = new ComboBox<>();
        tipoSala2.getItems().setAll(TipoSala.values());
        tipoSala2.setValue(TipoSala.DOS_D);
        tipoSala2.setMaxWidth(Double.MAX_VALUE);
        DatePicker fecha2 = new DatePicker(LocalDate.now().plusDays(1));
        Button refrescarPeliculas = boton("Actualizar catálogo de películas", "report-button");
        refrescarPeliculas.setOnAction(e -> peliculas.getItems().setAll(controller.peliculas()));
        Button programar = boton("Programar función", "secondary-button");
        programar.setOnAction(e -> ejecutar(() -> {
            if (peliculas.getValue() == null) throw new IllegalArgumentException("Selecciona una película.");
            controller.programarFuncion(peliculas.getValue(), obligatorio(sala2, "Escribe la sala."), tipoSala2.getValue(), fechaHora(fecha2, hora2),
                    numero(precio2, "Precio no válido."), entero(filas2, "Filas no válidas."), entero(columnas2, "Columnas no válidas."));
            mensaje.setText("Función programada correctamente.");
        }));
        box.getChildren().addAll(peliculas, refrescarPeliculas, sala2, tipoSala2, fecha2, hora2, precio2, filas2, columnas2, programar);
        return box;
    }

    private VBox crearGestionProductos() {
        VBox box = form();
        box.getChildren().add(subtitulo("Ingresar producto de confitería"));
        TextField nombre = campo("Nombre del producto"), precio = campo("Precio en COP"), stock = campo("Stock inicial");
        ListView<Producto> lista = new ListView<>();
        lista.getItems().setAll(controller.productos());
        lista.setPrefHeight(220);
        Button agregar = boton("Agregar producto", "primary-button");
        agregar.setOnAction(e -> ejecutar(() -> {
            controller.registrarProducto(obligatorio(nombre, "Escribe el nombre."), numero(precio, "Precio no válido."), entero(stock, "Stock no válido."));
            lista.getItems().setAll(controller.productos());
            nombre.clear();
            precio.clear();
            stock.clear();
            mensaje.setText("Producto agregado al catálogo.");
        }));
        box.getChildren().addAll(nombre, precio, stock, agregar, subtitulo("Productos actuales"), lista);
        return box;
    }

    private VBox crearGestionCombos() {
        VBox box = form();
        box.getChildren().add(subtitulo("Crear combo con productos existentes"));
        TextField nombre = campo("Nombre del combo");
        ListView<Producto> productos = new ListView<>();
        productos.getItems().setAll(controller.productos());
        productos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        productos.setPrefHeight(240);
        ListView<Combo> combos = new ListView<>();
        combos.getItems().setAll(controller.combos());
        combos.setPrefHeight(160);
        Button refrescar = boton("Actualizar productos y combos", "report-button");
        refrescar.setOnAction(e -> {
            productos.getItems().setAll(controller.productos());
            combos.getItems().setAll(controller.combos());
        });
        Button crear = boton("Crear combo con la selección", "primary-button");
        crear.setOnAction(e -> ejecutar(() -> {
            controller.registrarCombo(obligatorio(nombre, "Escribe el nombre del combo."), List.copyOf(productos.getSelectionModel().getSelectedItems()));
            combos.getItems().setAll(controller.combos());
            nombre.clear();
            productos.getSelectionModel().clearSelection();
            mensaje.setText("Combo agregado al catálogo.");
        }));
        box.getChildren().addAll(nombre, new Label("Selecciona uno o varios productos."), productos, refrescar, crear, subtitulo("Combos actuales"), combos);
        return box;
    }

    private VBox crearGestionPromociones() {
        VBox box = form();
        box.getChildren().add(subtitulo("Crear promoción para combos"));
        TextField nombre = campo("Nombre de la promoción"), descuento = campo("Porcentaje de descuento (0 a 100)");
        DatePicker inicio = new DatePicker(LocalDate.now()), fin = new DatePicker(LocalDate.now().plusDays(30));
        ListView<Promocion> lista = new ListView<>();
        lista.getItems().setAll(controller.promociones());
        lista.setPrefHeight(200);
        Button agregar = boton("Registrar promoción", "primary-button");
        agregar.setOnAction(e -> ejecutar(() -> {
            controller.registrarPromocion(obligatorio(nombre, "Escribe el nombre."), numero(descuento, "Descuento no válido."), inicio.getValue(), fin.getValue());
            lista.getItems().setAll(controller.promociones());
            nombre.clear();
            descuento.clear();
            mensaje.setText("Promoción registrada; se aplicará a los combos vigentes.");
        }));
        box.getChildren().addAll(nombre, descuento, new Label("Fecha de inicio"), inicio, new Label("Fecha de fin"), fin, agregar, subtitulo("Promociones actuales"), lista);
        return box;
    }

    private VBox crearRegistroVenta() {
        VBox box = form();
        box.getChildren().add(subtitulo("Registrar una compra para un cliente"));
        Label ayuda = new Label("La venta cobra la tarjeta del cliente y valida saldo, asientos, inventario y descuentos.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("movie-description");
        ComboBox<Cliente> cliente = new ComboBox<>();
        cliente.getItems().setAll(controller.clientesRegistrados());
        cliente.setPromptText("Cliente");
        cliente.setMaxWidth(Double.MAX_VALUE);
        cliente.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Cliente c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre() + " · Saldo $" + String.format("%,.0f", c.getTarjeta().getSaldo()));
            }
        });
        cliente.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cliente c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre() + " · Saldo $" + String.format("%,.0f", c.getTarjeta().getSaldo()));
            }
        });
        ComboBox<Funcion> funcion = new ComboBox<>();
        funcion.getItems().setAll(controller.todasLasFunciones());
        funcion.setPromptText("Función");
        funcion.setMaxWidth(Double.MAX_VALUE);
        ListView<Asiento> asientos = new ListView<>();
        asientos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        asientos.setPrefHeight(170);
        funcion.setOnAction(e -> {
            asientos.getItems().clear();
            if (funcion.getValue() != null)
                funcion.getValue().getAsientos().stream().filter(Asiento::estaDisponible).forEach(asientos.getItems()::add);
        });
        ListView<Producto> productos = new ListView<>();
        productos.getItems().setAll(controller.productos());
        productos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        productos.setPrefHeight(110);
        ListView<Combo> combos = new ListView<>();
        combos.getItems().setAll(controller.combos());
        combos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        combos.setPrefHeight(100);
        Button registrar = boton("Cobrar y registrar compra", "primary-button");
        registrar.setOnAction(e -> ejecutar(() -> {
            if (cliente.getValue() == null || funcion.getValue() == null)
                throw new IllegalArgumentException("Selecciona cliente y función.");
            if (asientos.getSelectionModel().getSelectedItems().isEmpty())
                throw new IllegalArgumentException("Selecciona al menos un asiento.");
            Compra compra = controller.registrarCompraAdministrador(cliente.getValue(), funcion.getValue(), List.copyOf(asientos.getSelectionModel().getSelectedItems()),
                    List.copyOf(productos.getSelectionModel().getSelectedItems()), List.copyOf(combos.getSelectionModel().getSelectedItems()));
            new Alert(Alert.AlertType.INFORMATION, "Compra registrada y cobrada.\nTotal: $" + String.format("%,.0f", compra.getTotal()) + " COP\nSaldo: $" + String.format("%,.0f", cliente.getValue().getTarjeta().getSaldo()) + " COP\nPuntos: " + cliente.getValue().getRegistroPuntos().consultarSaldo(), ButtonType.OK).showAndWait();
            asientos.getItems().removeIf(a -> !a.estaDisponible());
            mensaje.setText("Compra registrada para " + cliente.getValue().getNombre() + ".");
        }));
        box.getChildren().addAll(ayuda, cliente, funcion, subtitulo("Asientos disponibles (selecciona uno o más)"), asientos,
                subtitulo("Productos individuales (opcional)"), productos, subtitulo("Combos (opcional)"), combos, registrar);
        return box;
    }

    private VBox crearGestionCompras() {
        VBox box = form();
        box.getChildren().add(subtitulo("Compras registradas"));
        ListView<Compra> compras = new ListView<>();
        compras.setPrefHeight(240);
        compras.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Compra c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getFecha().toLocalDate() + " · " + c.getCliente().getNombre() + " · " + c.getFuncion().getPelicula().getTitulo() + " · " + c.getEntradas().size() + " entradas · $" + String.format("%,.0f", c.getTotal()) + " COP");
            }
        });
        TextArea reporte = new TextArea();
        reporte.setEditable(false);
        reporte.setWrapText(true);
        reporte.setPrefRowCount(7);
        reporte.getStyleClass().add("summary-area");
        Label total = new Label();
        total.getStyleClass().add("movie-description");
        ComboBox<TipoReporte> tipo = new ComboBox<>();
        tipo.getItems().setAll(TipoReporte.values());
        tipo.setValue(tipoReporteSeleccionado);
        tipo.setMaxWidth(Double.MAX_VALUE);
        ComboBox<FormatoReporte> formato = new ComboBox<>();
        formato.getItems().setAll(FormatoReporte.values());
        formato.setValue(formatoReporteSeleccionado);
        formato.setMaxWidth(Double.MAX_VALUE);
        tipo.setOnAction(e -> {
            if (tipo.getValue() != null) {
                tipoReporteSeleccionado = tipo.getValue();
                reporte.setText(controller.generarReporte(tipoReporteSeleccionado));
            }
        });
        formato.setOnAction(e -> {
            if (formato.getValue() != null) formatoReporteSeleccionado = formato.getValue();
        });
        Button actualizar = boton("Actualizar ventas", "secondary-button");
        actualizar.setOnAction(e -> refrescarCompras(compras, reporte, total));
        Button exportar = boton("Exportar reporte", "primary-button");
        exportar.setTooltip(new Tooltip("Guarda el reporte en el formato seleccionado."));
        exportar.setOnAction(e -> guardarReporte());
        refrescarCompras(compras, reporte, total);
        box.getChildren().addAll(total, compras, subtitulo("Tipo de reporte"), tipo, subtitulo("Formato"), formato, subtitulo("Vista previa del reporte"), reporte, actualizar, exportar);
        return box;
    }

    private void refrescarCompras(ListView<Compra> lista, TextArea reporte, Label total) {
        lista.getItems().setAll(controller.compras());
        reporte.setText(controller.generarReporte(tipoReporteSeleccionado));
        total.setText("Compras confirmadas: " + controller.compras().size() + " · Total vendido: $" + String.format("%,.0f", controller.compras().stream().mapToDouble(Compra::getTotal).sum()) + " COP");
    }


    private VBox crearGestionSolicitudesRecarga() {
        VBox box = form();
        box.getChildren().add(subtitulo("Solicitudes de recarga pendientes"));
        ListView<SolicitudRecarga> lista = new ListView<>();
        lista.setPrefHeight(300);
        lista.getItems().setAll(controller.solicitudesRecarga());
        Button actualizar = boton("Actualizar solicitudes", "secondary-button");
        actualizar.setOnAction(e -> lista.getItems().setAll(controller.solicitudesRecarga()));
        Button aprobar = boton("Aprobar y acreditar saldo", "primary-button");
        aprobar.setOnAction(e -> ejecutar(() -> {
            if (lista.getSelectionModel().getSelectedItem() == null)
                throw new IllegalArgumentException("Selecciona una solicitud.");
            controller.aprobarRecarga(lista.getSelectionModel().getSelectedItem().getId());
            lista.getItems().setAll(controller.solicitudesRecarga());
            mensaje.setText("Recarga aprobada y movimiento registrado en la tarjeta.");
        }));
        Button rechazar = boton("Rechazar solicitud", "report-button");
        rechazar.setOnAction(e -> ejecutar(() -> {
            if (lista.getSelectionModel().getSelectedItem() == null)
                throw new IllegalArgumentException("Selecciona una solicitud.");
            controller.rechazarRecarga(lista.getSelectionModel().getSelectedItem().getId());
            lista.getItems().setAll(controller.solicitudesRecarga());
            mensaje.setText("Solicitud rechazada.");
        }));
        box.getChildren().addAll(lista, actualizar, aprobar, rechazar);
        return box;
    }

    private VBox crearGestionSalas() {
        VBox box = form();
        box.getChildren().add(subtitulo("Administración independiente de salas"));
        TextField id = campo("Nombre / identificador, por ejemplo Sala 4"), filas = campo("Filas"), columnas = campo("Columnas");
        ComboBox<TipoSala> tipo = new ComboBox<>();
        tipo.getItems().setAll(TipoSala.values());
        tipo.setValue(TipoSala.DOS_D);
        tipo.setMaxWidth(Double.MAX_VALUE);
        ListView<Sala> lista = new ListView<>();
        lista.setPrefHeight(200);
        lista.getItems().setAll(controller.salas());
        Button crear = boton("Crear sala", "primary-button");
        crear.setOnAction(e -> ejecutar(() -> {
            Sala nueva = controller.registrarSala(obligatorio(id, "Escribe el identificador."), entero(filas, "Filas inválidas."), entero(columnas, "Columnas inválidas."), tipo.getValue());
            lista.getItems().setAll(controller.salas());
            mensaje.setText("Sala creada: " + nueva.getIdSala() + " · " + nueva.getTipoSala() + " · " + nueva.getCapacidad() + " asientos.");
        }));
        Button eliminar = boton("Eliminar sala seleccionada", "report-button");
        eliminar.setOnAction(e -> ejecutar(() -> {
            Sala s = lista.getSelectionModel().getSelectedItem();
            if (s == null) throw new IllegalArgumentException("Selecciona una sala.");
            controller.eliminarSala(s.getIdSala());
            lista.getItems().setAll(controller.salas());
        }));
        box.getChildren().addAll(id, filas, columnas, tipo, crear, lista, eliminar);
        return box;
    }

    private VBox crearRecargaTarjeta() {
        VBox box = form();
        box.getChildren().add(subtitulo("Recargar tarjeta virtual de un cliente"));
        Label ayuda = new Label("El monto se suma al saldo y queda registrado como un movimiento de recarga en la tarjeta.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("movie-description");
        ComboBox<Cliente> cliente = new ComboBox<>();
        cliente.getItems().setAll(controller.clientesRegistrados());
        cliente.setPromptText("Selecciona un cliente");
        cliente.setMaxWidth(Double.MAX_VALUE);
        cliente.setCellFactory(v -> celdaClienteSaldo());
        cliente.setButtonCell(celdaClienteSaldo());
        Label saldo = new Label("Saldo actual: selecciona un cliente");
        saldo.getStyleClass().add("wallet-label");
        cliente.setOnAction(e -> {
            if (cliente.getValue() != null)
                saldo.setText("Saldo actual: $" + String.format("%,.0f", cliente.getValue().getTarjeta().getSaldo()) + " COP");
        });
        TextField monto = campo("Monto de la recarga en COP");
        Button recargar = boton("Recargar tarjeta", "primary-button");
        recargar.setOnAction(e -> ejecutar(() -> {
            if (cliente.getValue() == null) throw new IllegalArgumentException("Selecciona el cliente.");
            double valor = numero(monto, "Escribe un monto de recarga válido.");
            if (valor <= 0) throw new IllegalArgumentException("La recarga debe ser mayor que cero.");
            double nuevoSaldo = controller.recargarTarjeta(cliente.getValue(), valor);
            saldo.setText("Saldo actualizado: $" + String.format("%,.0f", nuevoSaldo) + " COP");
            monto.clear();
            new Alert(Alert.AlertType.INFORMATION, "Recarga registrada: $" + String.format("%,.0f", valor) + " COP\nNuevo saldo: $" + String.format("%,.0f", nuevoSaldo) + " COP", ButtonType.OK).showAndWait();
            mensaje.setText("Tarjeta recargada para " + cliente.getValue().getNombre() + ".");
        }));
        Button actualizar = boton("Actualizar lista de clientes", "report-button");
        actualizar.setOnAction(e -> cliente.getItems().setAll(controller.clientesRegistrados()));
        box.getChildren().addAll(ayuda, cliente, saldo, monto, recargar, actualizar);
        return box;
    }

    private ListCell<Cliente> celdaClienteSaldo() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Cliente c, boolean vacio) {
                super.updateItem(c, vacio);
                setText(vacio || c == null ? null : c.getNombre() + " · " + c.getEmail() + " · $" + String.format("%,.0f", c.getTarjeta().getSaldo()) + " COP");
            }
        };
    }

    private VBox crearGestionClientes() {
        VBox box = form();
        box.getChildren().add(subtitulo("Clientes registrados"));
        ListView<Cliente> lista = new ListView<>();
        lista.setPrefHeight(340);
        lista.getItems().setAll(controller.clientesRegistrados());
        lista.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Cliente c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre() + " · " + c.getEmail() + " · Saldo $" + String.format("%,.0f", c.getTarjeta().getSaldo()) + " COP · Puntos " + c.getRegistroPuntos().consultarSaldo());
            }
        });
        box.getChildren().add(lista);
        return box;
    }

    private TextField campo(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    private Button boton(String texto, String estilo) {
        Button button = new Button(texto);
        button.getStyleClass().add(estilo);
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private String obligatorio(TextInputControl campo, String error) {
        String valor = campo.getText() == null ? "" : campo.getText().trim();
        if (valor.isEmpty()) throw new IllegalArgumentException(error);
        return valor;
    }

    private int entero(TextField campo, String error) {
        try {
            int valor = Integer.parseInt(obligatorio(campo, error));
            if (valor < 1) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(error);
        }
    }

    private double numero(TextField campo, String error) {
        try {
            double valor = Double.parseDouble(obligatorio(campo, error));
            if (!Double.isFinite(valor) || valor < 0) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(error);
        }
    }

    private LocalDateTime fechaHora(DatePicker fecha, TextField hora) {
        if (fecha.getValue() == null) throw new IllegalArgumentException("Selecciona la fecha.");
        try {
            return LocalDateTime.of(fecha.getValue(), LocalTime.parse(obligatorio(hora, "Escribe la hora en formato HH:mm.")));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La hora debe tener formato HH:mm.");
        }
    }

    private void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException ex) {
            mensaje.setText(ex.getMessage());
        }
    }

    private void guardarReporte() {
        String extension = formatoReporteSeleccionado == FormatoReporte.PDF ? "pdf" : "txt";
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte");
        chooser.setInitialFileName("reporte-cinemax." + extension);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo " + extension.toUpperCase(java.util.Locale.ROOT), "*." + extension));
        java.io.File archivo = chooser.showSaveDialog(root.getScene().getWindow());
        if (archivo == null) return;
        try {
            controller.exportarReporte(archivo.toPath(), new SolicitudReporte(tipoReporteSeleccionado, formatoReporteSeleccionado, null, null));
            new Alert(Alert.AlertType.INFORMATION, "Reporte guardado correctamente.", ButtonType.OK).showAndWait();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "No se pudo guardar el reporte: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}






