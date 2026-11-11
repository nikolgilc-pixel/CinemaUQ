package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;

public class ReembolsoPorTiempo implements IPoliticaReembolso{
    @Override
    public double calcularMonto(double total, LocalDateTime fechaFuncion, LocalDateTime fechaSolicitud) {
        if (fechaSolicitud.plusHours(2).isAfter(fechaFuncion))
            throw new CompraInvalidaException("Solo puedes cancelar hasta 2 horas antes de la función.");
        return total;
    }

}
