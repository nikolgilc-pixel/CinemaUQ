package co.edu.uniquindio.poo.model;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportePDF implements IReporte {
    private static final int LINEAS_POR_PAGINA = 52;
    private static final int ANCHO_LINEA = 88;

    @Override
    public String generar(String datos) {
        List<String> lineas = prepararLineas(datos == null ? "" : datos);
        List<String> paginas = new ArrayList<>();
        for (int inicio = 0; inicio < lineas.size(); inicio += LINEAS_POR_PAGINA) {
            StringBuilder stream = new StringBuilder("BT /F1 10 Tf 50 790 Td 13 TL\n");
            int fin = Math.min(inicio + LINEAS_POR_PAGINA, lineas.size());
            for (int i = inicio; i < fin; i++)
                stream.append('(').append(escapar(lineas.get(i))).append(") Tj T*\n");
            stream.append("ET");
            paginas.add(stream.toString());
        }
        if (paginas.isEmpty()) paginas.add("BT /F1 10 Tf 50 790 Td ET");

        List<String> objetos = new ArrayList<>();
        objetos.add("<< /Type /Catalog /Pages 2 0 R >>");
        objetos.add(""); // El árbol de páginas se completa cuando se conocen sus referencias.
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        List<Integer> referenciasPaginas = new ArrayList<>();
        for (int i = 0; i < paginas.size(); i++) {
            int numeroPagina = 4 + i * 2;
            int numeroContenido = numeroPagina + 1;
            referenciasPaginas.add(numeroPagina);
            objetos.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + numeroContenido + " 0 R >>");
            String contenido = paginas.get(i);
            objetos.add("<< /Length " + contenido.getBytes(StandardCharsets.US_ASCII).length
                    + " >>\nstream\n" + contenido + "\nendstream");
        }
        StringBuilder hijos = new StringBuilder();
        for (int referencia : referenciasPaginas) hijos.append(referencia).append(" 0 R ");
        objetos.set(1, "<< /Type /Pages /Kids [" + hijos + "] /Count " + paginas.size() + " >>");

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objetos.size(); i++) {
            offsets.add(pdf.length());
            pdf.append(i + 1).append(" 0 obj\n").append(objetos.get(i)).append("\nendobj\n");
        }
        int xref = pdf.length();
        pdf.append("xref\n0 ").append(objetos.size() + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) pdf.append(String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        pdf.append("trailer\n<< /Size ").append(objetos.size() + 1)
                .append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF");
        return pdf.toString();
    }

    private List<String> prepararLineas(String texto) {
        List<String> resultado = new ArrayList<>();
        for (String original : texto.replace("\r", "").split("\n", -1)) {
            String linea = Normalizer.normalize(original, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").replaceAll("[^ -~]", "?");
            if (linea.isEmpty()) {
                resultado.add("");
                continue;
            }
            while (linea.length() > ANCHO_LINEA) {
                int corte = linea.lastIndexOf(' ', ANCHO_LINEA);
                if (corte < 1) corte = ANCHO_LINEA;
                resultado.add(linea.substring(0, corte));
                linea = linea.substring(corte).stripLeading();
            }
            resultado.add(linea);
        }
        return resultado;
    }

    private String escapar(String linea) {
        return linea.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
