package co.edu.uniquindio.poo.view;

import co.edu.uniquindio.poo.controller.CinemaController;
import co.edu.uniquindio.poo.model.Cliente;
import co.edu.uniquindio.poo.model.Funcion;
import co.edu.uniquindio.poo.view.AccesoClienteView;
import co.edu.uniquindio.poo.view.CarteleraView;
import co.edu.uniquindio.poo.view.CinemaView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Aplicación JavaFX del sistema CinemaUQ.
 */
public class CinemaApp extends Application {
    private final CinemaController controller = new CinemaController();
    private Scene scene;
    private Funcion funcionPendiente;

    @Override
    public void start(Stage stage) {
        scene = new Scene(new StackPane(), 1240, 900);
        URL estilos = getClass().getResource("/css/cinema.css");
        if (estilos != null) scene.getStylesheets().add(estilos.toExternalForm());
        stage.setTitle("CinemaUQ · Vive el cine");
        stage.setMinWidth(980);
        stage.setMinHeight(720);
        stage.setScene(scene);
        mostrarCartelera();
        stage.show();
    }

    private void mostrar(Parent pantalla) {
        scene.setRoot(pantalla);
    }

    private void mostrarCartelera() {
        CarteleraView vista = new CarteleraView(controller, this::comprarFuncion, this::abrirAccesoCliente);
        mostrar(vista.getRoot());
    }

    private void comprarFuncion(Funcion funcion) {
        if (!controller.clienteAutenticado()) {
            funcionPendiente = funcion;
            abrirAccesoCliente();
            return;
        }
        funcionPendiente = null;
        controller.setFuncion(funcion);
        mostrarCompra();
    }

    private void abrirAccesoCliente() {
        if (controller.clienteAutenticado()) {
            mostrarCompra();
            return;
        }
        AccesoClienteView vista = new AccesoClienteView(controller,
                this::cancelarAccesoCliente, this::clienteIngresado);
        mostrar(vista.getRoot());
    }

    private void cancelarAccesoCliente() {
        funcionPendiente = null;
        mostrarCartelera();
    }

    private void clienteIngresado(Cliente cliente) {
        if (funcionPendiente != null) {
            controller.setFuncion(funcionPendiente);
            funcionPendiente = null;
            mostrarCompra();
        } else {
            mostrarCartelera();
        }
    }

    private void mostrarCompra() {
        CinemaView vista = new CinemaView(controller, this::mostrarCartelera);
        mostrar(vista.getRoot());
    }
}


