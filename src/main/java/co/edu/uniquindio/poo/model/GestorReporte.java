package co.edu.uniquindio.poo.model;

import java.util.List;
import java.util.Objects;

public class GestorReporte {
    private final GestorCompra gestorCompra;
    private final GestorUsuario gestorUsuario;
    private final ReporteVentas reporteVentas;
    private CreadorReporte creador;

    public GestorReporte(GestorCompra gestorCompra) {
        this(gestorCompra, null, new ReporteVentas());
    }

    public GestorReporte(GestorCompra gestorCompra, ReporteVentas reporteVentas) {
        this(gestorCompra, null, reporteVentas);
    }

    public GestorReporte(GestorCompra gestorCompra, GestorUsuario gestorUsuario) {
        this(gestorCompra, gestorUsuario, new ReporteVentas());
    }

    private GestorReporte(GestorCompra gestorCompra, GestorUsuario gestorUsuario, ReporteVentas reporteVentas) {
        this.gestorCompra = Objects.requireNonNull(gestorCompra, "El gestor de compras es obligatorio.");
        this.gestorUsuario = gestorUsuario;
        this.reporteVentas = Objects.requireNonNull(reporteVentas, "El generador de datos del reporte es obligatorio.");
    }

    public synchronized String generarReporte(SolicitudReporte solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud de reporte es obligatoria.");
        List<Compra> compras = gestorCompra.consultarCompras().stream()
                .filter(compra -> solicitud.incluye(compra.getFecha().toLocalDate())).toList();
        List<Cliente> clientes = gestorUsuario == null ? List.of() : gestorUsuario.listarClientes();
        String datos = reporteVentas.generar(compras, solicitud.getTipo(), clientes);
        creador = solicitud.getFormato() == FormatoReporte.PDF
                ? new CreadorReportePDF() : new CreadorReporteTXT();
        return creador.exportar(datos);
    }

    public synchronized CreadorReporte getCreador() {
        return creador;
    }
}
