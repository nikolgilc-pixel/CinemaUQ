package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.model.Compra;
import co.edu.uniquindio.poo.model.IObservadorCompra;
import co.edu.uniquindio.poo.model.ReporteVentas;
import co.edu.uniquindio.poo.model.TipoReporte;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Operaciones de administración del historial y los reportes de compra. */
public final class ControladorPanelAdminCompras implements IObservadorCompra {
    private final CinemaController cine;
    private final ReporteVentas estadisticas = new ReporteVentas();
    private List<Compra> compras = List.of();
    private Runnable alCambiar = () -> { };

    public ControladorPanelAdminCompras(CinemaController cine) {
        this.cine = Objects.requireNonNull(cine);
        refrescar();
    }

    public List<Compra> consultarCompras() {
        return compras;
    }

    public double totalVentasConfirmadas() {
        return consultarCompras().stream().filter(Compra::estaConfirmada)
                .mapToDouble(Compra::getTotal).sum();
    }

    public Map<String, Double> ventasPorPelicula() {
        return estadisticas.ventasPorPelicula(compras);
    }

    public Map<String, Integer> entradasPorPelicula() {
        return estadisticas.entradasPorPelicula(compras);
    }

    public Map<String, Double> ventasPorDia() {
        return estadisticas.ventasPorDia(compras);
    }

    public String generarReporte(TipoReporte tipo) {
        return cine.generarReporte(tipo);
    }

    public Compra reembolsarPorCancelacionDelCine(String idCompra) {
        return cine.cancelarCompraPorCine(idCompra);
    }

    public void setAlCambiar(Runnable alCambiar) {
        this.alCambiar = Objects.requireNonNull(alCambiar);
    }

    private void refrescar() {
        compras = cine.compras();
    }

    @Override
    public void actualizar(Compra compra) {
        refrescar();
        alCambiar.run();
    }
}
