package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;

public interface IPoliticaReembolso {
    double calcularMonto(double total, LocalDateTime fechaFuncion, LocalDateTime fechaSolicitud);
}
