package co.edu.uniquindio.poo.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GestorTarjeta {
    private final Map<String, TarjetaVirtual> tarjetas = new LinkedHashMap<>();

    public synchronized TarjetaVirtual registrar(TarjetaVirtual tarjeta) {
        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria.");
        if (tarjetas.putIfAbsent(tarjeta.getNumero(), tarjeta) != null)
            throw new IllegalArgumentException("Ya existe una tarjeta con ese número.");
        return tarjeta;
    }

    public synchronized TarjetaVirtual buscar(String numero) {
        return tarjetas.get(numero);
    }

    public synchronized List<TarjetaVirtual> listar() {
        return List.copyOf(tarjetas.values());
    }

    public synchronized double recargar(TarjetaVirtual tarjeta, double monto) {
        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria.");
        TarjetaVirtual registrada = tarjetas.get(tarjeta.getNumero());
        if (registrada == null) registrar(tarjeta);
        tarjeta.recargar(monto);
        return tarjeta.getSaldo();
    }
}
