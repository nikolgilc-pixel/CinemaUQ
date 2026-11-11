package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;

public class ReembolsoPorCancelacionCine implements IPoliticaReembolso{
    @Override
    public double calcularMonto(double total, LocalDateTime fechaFuncion, LocalDateTime fechaSolicitud) {
        return total;
    }
}
