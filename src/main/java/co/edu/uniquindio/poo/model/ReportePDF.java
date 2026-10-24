package co.edu.uniquindio.poo.model;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportePDF implements IReporte {
    @Override
    public String generar(String datos) {
        StringBuilder stream = new StringBuilder("BT /F1 10 Tf 50 790 Td 13 TL\n");
        String contenido = datos == null ? "" : datos;
        for (String linea : contenido.replace("\r", "").split("\n")) {
            String segura = Normalizer.normalize(linea, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                    .replaceAll("[^ -~]", "?").replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
            stream.append('(').append(segura).append(") Tj T*\n");
        }
        stream.append("ET");
        String cuerpo = stream.toString();
        String[] objetos = {
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length " + cuerpo.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + cuerpo + "\nendstream"
        };
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objetos.length; i++) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.US_ASCII).length);
            pdf.append(i + 1).append(" 0 obj\n").append(objetos[i]).append("\nendobj\n");
        }
        int xref = pdf.toString().getBytes(StandardCharsets.US_ASCII).length;
        pdf.append("xref\n0 ").append(objetos.length + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) pdf.append(String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        pdf.append("trailer\n<< /Size ").append(objetos.length + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xref).append("\n%%EOF");
        return pdf.toString();
    }
}
