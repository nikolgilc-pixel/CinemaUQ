package co.edu.uniquindio.poo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestorPromocion {
    private final List<Promocion> promociones = new ArrayList<>();

    public GestorPromocion(Promocion inicial) {
        if (inicial != null) promociones.add(inicial);
    }

    public synchronized List<Promocion> consultarPromociones() {
        return List.copyOf(promociones);
    }

    public synchronized void agregarPromocion(Promocion promocion) {
        if (promocion == null) throw new IllegalArgumentException("La promoción no puede estar vacía.");
        promociones.add(promocion);
    }

    public synchronized Optional<IComponenteConfiteria> aplicarPromocion(IComponenteConfiteria componente) {
        return promociones.stream().filter(p -> p.estaVigente(LocalDate.now())).reduce((anterior, actual) -> actual)
                .map(p -> (IComponenteConfiteria) new ComboConPromocion(componente, p));
    }

}
