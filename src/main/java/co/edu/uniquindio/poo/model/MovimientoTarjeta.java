package co.edu.uniquindio.poo.model;

import java.time.LocalDateTime;

public record  MovimientoTarjeta (String tipo, double monto, double saldoResultante, LocalDateTime fechaHora){
}
