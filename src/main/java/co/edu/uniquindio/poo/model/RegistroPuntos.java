package co.edu.uniquindio.poo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;

public class RegistroPuntos {
    private final List<MovimientoPuntos> historial = new ArrayList<>();

    public int acumular(double valorPagado) {
        return acumularCompra(valorPagado).puntos();
    }

    public MovimientoPuntos acumularCompra(double valorPagado) {
        int p = (int) (valorPagado / 1000);
        MovimientoPuntos movimiento = new MovimientoPuntos(p, LocalDate.now());
        if (p > 0) historial.add(movimiento);
        return movimiento;
    }

    public void revertirMovimiento(String id) {
        historial.removeIf(m -> m.id().equals(id));
    }

    public void recompensarAsistencia() {
        historial.add(new MovimientoPuntos(20, LocalDate.now()));
    }


    public int consultarSaldo() {
        return historial.stream().filter(MovimientoPuntos::estaVigente).mapToInt(MovimientoPuntos::puntos).sum();
    }

    public List<MovimientoPuntos> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public boolean canjear(int puntos) {
        if (puntos <= 0 || consultarSaldo() < puntos) return false;
        int restantes = puntos;
        java.util.List<Integer> prioridad = java.util.stream.IntStream.range(0, historial.size()).boxed()
                .filter(i -> historial.get(i).estaVigente())
                .sorted(java.util.Comparator.comparing(i -> historial.get(i).fechaVencimiento())).toList();
        for (int i : prioridad) {
            if (restantes == 0) break;
            MovimientoPuntos m = historial.get(i);
            if (!m.estaVigente()) continue;
            int uso = Math.min(restantes, m.puntos());
            if (uso > 0) {
                historial.set(i, m.conPuntos(m.puntos() - uso));
                restantes -= uso;
            }
        }
        return restantes == 0;
    }
}
