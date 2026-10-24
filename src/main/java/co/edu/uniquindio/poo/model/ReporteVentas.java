package co.edu.uniquindio.poo.model;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteVentas {
    public String generar(List<Compra> compras) {
        return generar(compras, TipoReporte.VENTAS_POR_PERIODO);
    }

    public String generar(List<Compra> compras, TipoReporte tipo) {
        String titulo = switch (tipo) {
            case VENTAS_POR_PERIODO -> "VENTAS POR PERIODO";
            case ENTRADAS_POR_PELICULA_O_FUNCION -> "ENTRADAS POR PELÍCULA O FUNCIÓN";
            case OCUPACION_SALAS -> "OCUPACIÓN DE SALAS";
            case PRODUCTOS_Y_COMBOS_VENDIDOS -> "PRODUCTOS Y COMBOS VENDIDOS";
            case RECARGAS -> "RECARGAS";
            case CANCELACIONES_Y_REEMBOLSOS -> "CANCELACIONES Y REEMBOLSOS";
            case PUNTOS_ACUMULADOS_Y_REDIMIDOS -> "PUNTOS ACUMULADOS Y REDIMIDOS";
        };
        StringBuilder texto = new StringBuilder("REPORTE CINEMAUQ · ").append(titulo).append('\n');
        texto.append("========================================\n");
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        switch (tipo) {
            case VENTAS_POR_PERIODO -> {
                for (Compra compra : compras) {
                    texto.append(compra.getFecha().format(formato)).append(" · ")
                            .append(compra.getCliente().getNombre()).append(" · ")
                            .append(compra.getFuncion().getPelicula().getTitulo()).append(" · $")
                            .append(String.format("%,.0f", compra.getTotal())).append('\n');
                }
                texto.append("Total vendido: $").append(String.format("%,.0f", compras.stream().mapToDouble(Compra::getTotal).sum())).append('\n');
            }
            case ENTRADAS_POR_PELICULA_O_FUNCION ->
                    compras.forEach(c -> texto.append(c.getFuncion().getPelicula().getTitulo())
                            .append(" · ").append(c.getFuncion().getId()).append(" · ").append(c.getEntradas().size()).append(" entradas\n"));
            case PRODUCTOS_Y_COMBOS_VENDIDOS ->
                    compras.forEach(c -> c.getDetalles().forEach(d -> texto.append(d.cantidad()).append(" × ")
                            .append(d.descripcion()).append(" · $").append(String.format("%,.0f", d.subtotal())).append('\n')));
            case OCUPACION_SALAS -> compras.forEach(c -> texto.append(c.getFuncion().getSala()).append(" · ")
                    .append(c.getFuncion().getPelicula().getTitulo()).append(" · ").append(c.getEntradas().size()).append(" entradas vendidas\n"));
            case RECARGAS, CANCELACIONES_Y_REEMBOLSOS, PUNTOS_ACUMULADOS_Y_REDIMIDOS ->
                    texto.append("No hay movimientos de este tipo disponibles en el registro de compras.\n");
        }
        if (compras.isEmpty()) texto.append("Aún no hay ventas registradas.\n");
        return texto.toString();
    }

    public void exportar(java.nio.file.Path destino, FormatoReporte formato, String contenido) throws java.io.IOException {
        if (formato == FormatoReporte.TXT) {
            java.nio.file.Files.writeString(destino, contenido, java.nio.charset.StandardCharsets.UTF_8);
            return;
        }
        String pdf = new ReportePDF().generar(contenido);
        java.nio.file.Files.writeString(destino, pdf, java.nio.charset.StandardCharsets.US_ASCII);
    }
}
