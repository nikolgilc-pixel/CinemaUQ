package co.edu.uniquindio.poo.model;

import java.time.format.DateTimeFormatter;

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
        String texto = contenido.replace("\r", "");
        StringBuilder stream = new StringBuilder("BT /F1 10 Tf 50 790 Td 13 TL\n");
        for (String linea : texto.split("\n")) {
            String segura = java.text.Normalizer.normalize(linea, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").replaceAll("[^ -~]", "?")
                    .replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
            stream.append('(').append(segura).append(") Tj T*\n");
        }
        stream.append("ET");
        String contenidoPdf = stream.toString();
        String[] objetos = {
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length " + contenidoPdf.getBytes(java.nio.charset.StandardCharsets.US_ASCII).length + " >>\nstream\n" + contenidoPdf + "\nendstream"
        };
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        java.util.List<Integer> offsets = new java.util.ArrayList<>();
        for (int i = 0; i < objetos.length; i++) {
            offsets.add(pdf.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII).length);
            pdf.append(i + 1).append(" 0 obj\n").append(objetos[i]).append("\nendobj\n");
        }
        int xref = pdf.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII).length;
        pdf.append("xref\n0 ").append(objetos.length + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) pdf.append(String.format(java.util.Locale.ROOT, "%010d 00000 n \n", offset));
        pdf.append("trailer\n<< /Size ").append(objetos.length + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xref).append("\n%%EOF");
        java.nio.file.Files.write(destino, pdf.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }
}
