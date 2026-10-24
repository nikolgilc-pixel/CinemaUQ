package co.edu.uniquindio.poo.model;

import java.util.List;
import java.util.Objects;

public class GestorReporte {
    private final GestorCompra gestorCompra;
    private final ReporteVentas reporteVentas;
    private CreadorReporte creador;

    public GestorReporte(GestorCompra gestorCompra) {
        this(gestorCompra, new ReporteVentas());
    }

    public GestorReporte(GestorCompra gestorCompra, ReporteVentas reporteVentas) {
        this.gestorCompra = Objects.requireNonNull(gestorCompra, "El gestor de compras es obligatorio.");
        this.reporteVentas = Objects.requireNonNull(reporteVentas, "El generador de datos del reporte es obligatorio.");
    }

    public synchronized String generarReporte(SolicitudReporte solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud de reporte es obligatoria.");
        List<Compra> compras = gestorCompra.consultarCompras().stream()
                .filter(compra -> solicitud.incluye(compra.getFecha().toLocalDate())).toList();
        String datos = reporteVentas.generar(compras, solicitud.getTipo());
        creador = solicitud.getFormato() == FormatoReporte.PDF
                ? new CreadorReportePDF() : new CreadorReporteTXT();
        return creador.exportar(datos);
    }

    public synchronized CreadorReporte getCreador() {
        return creador;
    }
}
