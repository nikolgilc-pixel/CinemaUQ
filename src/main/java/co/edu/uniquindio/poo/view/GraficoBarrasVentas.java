package co.edu.uniquindio.poo.view;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Map;

public final class GraficoBarrasVentas extends BarChart<String, Number> {

    public GraficoBarrasVentas() {
        super(new CategoryAxis(), new NumberAxis());
        getXAxis().setLabel("Película");
        getYAxis().setLabel("Ventas (COP)");
        setTitle("Ventas confirmadas por película");
        setLegendVisible(false);
        setAnimated(false);
        setPrefHeight(260);
        setMinWidth(300);
    }

    public void mostrar(Map<String, Double> ventasPorPelicula) {
        getData().clear();
        if (ventasPorPelicula == null || ventasPorPelicula.isEmpty()) return;

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        ventasPorPelicula.forEach((pelicula, total) ->
                serie.getData().add(new XYChart.Data<>(pelicula, total)));
        getData().add(serie);
    }
}
