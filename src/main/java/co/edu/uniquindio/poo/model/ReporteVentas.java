package co.edu.uniquindio.poo.model;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class ReporteVentas {
    public Map<String, Double> ventasPorPelicula(Collection<Compra> compras) {
        Map<String, Double> resultado = new TreeMap<>();
        comprasConfirmadas(compras).forEach(compra -> resultado.merge(
                compra.getFuncion().getPelicula().getTitulo(), compra.getTotal(), Double::sum));
        return Collections.unmodifiableMap(resultado);
    }

    public Map<String, Integer> entradasPorPelicula(Collection<Compra> compras) {
        Map<String, Integer> resultado = new TreeMap<>();
        comprasConfirmadas(compras).forEach(compra -> resultado.merge(
                compra.getFuncion().getPelicula().getTitulo(), compra.getEntradas().size(), Integer::sum));
        return Collections.unmodifiableMap(resultado);
    }

    public Map<String, Double> ventasPorDia(Collection<Compra> compras) {
        Map<String, Double> resultado = new TreeMap<>();
        comprasConfirmadas(compras).forEach(compra -> resultado.merge(
                compra.getFecha().toLocalDate().toString(), compra.getTotal(), Double::sum));
        return Collections.unmodifiableMap(resultado);
    }

    private List<Compra> comprasConfirmadas(Collection<Compra> compras) {
        if (compras == null) return List.of();
        return compras.stream().filter(Objects::nonNull).filter(Compra::estaConfirmada).toList();
    }

    public String generar(List<Compra> compras) {
        return generar(compras, TipoReporte.VENTAS_POR_PERIODO);
    }

    public String generar(List<Compra> compras, TipoReporte tipo) {
        return generar(compras, tipo, List.of());
    }

    public String generar(List<Compra> compras, TipoReporte tipo, Collection<Cliente> clientes) {
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
        List<Compra> confirmadas = compras.stream().filter(Compra::estaConfirmada).toList();
        switch (tipo) {
            case VENTAS_POR_PERIODO -> {
                for (Compra compra : confirmadas) {
                    texto.append(compra.getFecha().format(formato)).append(" · ")
                            .append(compra.getCliente().getNombre()).append(" · ")
                            .append(compra.getFuncion().getPelicula().getTitulo()).append(" · $")
                            .append(String.format("%,.0f", compra.getTotal())).append('\n');
                }
                texto.append("Total vendido: $").append(String.format("%,.0f", confirmadas.stream().mapToDouble(Compra::getTotal).sum())).append('\n');
            }
            case ENTRADAS_POR_PELICULA_O_FUNCION ->
                    confirmadas.forEach(c -> texto.append(c.getFuncion().getPelicula().getTitulo())
                            .append(" · ").append(c.getFuncion().getId()).append(" · ").append(c.getEntradas().size()).append(" entradas\n"));
            case PRODUCTOS_Y_COMBOS_VENDIDOS ->
                    confirmadas.forEach(c -> c.getDetalles().forEach(d -> texto.append(d.cantidad()).append(" × ")
                            .append(d.descripcion()).append(" · $").append(String.format("%,.0f", d.subtotal())).append('\n')));
            case OCUPACION_SALAS -> confirmadas.forEach(c -> texto.append(c.getFuncion().getSala()).append(" · ")
                    .append(c.getFuncion().getPelicula().getTitulo()).append(" · ").append(c.getEntradas().size()).append(" entradas vendidas\n"));
            case RECARGAS -> {
                int movimientos = 0;
                for (Cliente cliente : clientes) for (MovimientoTarjeta m : cliente.getTarjeta().getMovimientos()) {
                    if (m.tipo().equals("RECARGA")) {
                        movimientos++;
                        texto.append(m.fechaHora().format(formato)).append(" · ").append(cliente.getNombre())
                                .append(" · $ ").append(String.format("%,.0f", m.monto())).append('\n');
                    }
                }
                if (movimientos == 0) texto.append("Aún no hay recargas registradas.\n");
            }
            case CANCELACIONES_Y_REEMBOLSOS -> {
                List<Compra> canceladas = compras.stream().filter(Compra::isCancelada).toList();
                canceladas.forEach(c -> texto.append(c.getFecha().format(formato)).append(" · Compra ")
                        .append(c.getId()).append(" · ").append(c.getCliente().getNombre()).append(" · Cancelada\n"));
                double reembolsado = 0;
                for (Cliente cliente : clientes) for (MovimientoTarjeta m : cliente.getTarjeta().getMovimientos()) {
                    if (m.tipo().equals("REEMBOLSO")) {
                        reembolsado += m.monto();
                        texto.append(m.fechaHora().format(formato)).append(" · Reembolso a ").append(cliente.getNombre())
                                .append(" · $ ").append(String.format("%,.0f", m.monto())).append('\n');
                    }
                }
                texto.append("Compras canceladas: ").append(canceladas.size()).append('\n')
                        .append("Total reembolsado: $ ").append(String.format("%,.0f", reembolsado)).append('\n');
                if (canceladas.isEmpty() && reembolsado == 0) texto.append("Aún no hay cancelaciones ni reembolsos.\n");
            }
            case PUNTOS_ACUMULADOS_Y_REDIMIDOS -> {
                int acumulados = 0, redimidos = 0, vigentes = 0;
                for (Cliente cliente : clientes) {
                    RegistroPuntos puntos = cliente.getRegistroPuntos();
                    acumulados += puntos.getPuntosAcumulados();
                    redimidos += puntos.getPuntosRedimidos();
                    vigentes += puntos.consultarSaldo();
                    texto.append(cliente.getNombre()).append(" · acumulados: ").append(puntos.getPuntosAcumulados())
                            .append(" · redimidos: ").append(puntos.getPuntosRedimidos())
                            .append(" · vigentes: ").append(puntos.consultarSaldo()).append('\n');
                }
                texto.append("Total acumulados: ").append(acumulados).append(" · total redimidos: ").append(redimidos)
                        .append(" · saldo vigente: ").append(vigentes).append('\n');
                if (clientes.isEmpty()) texto.append("Aún no hay clientes con movimientos de puntos.\n");
            }
        }
        if (compras.isEmpty() && (tipo == TipoReporte.VENTAS_POR_PERIODO
                || tipo == TipoReporte.ENTRADAS_POR_PELICULA_O_FUNCION
                || tipo == TipoReporte.OCUPACION_SALAS
                || tipo == TipoReporte.PRODUCTOS_Y_COMBOS_VENDIDOS)) texto.append("Aún no hay ventas registradas.\n");
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
