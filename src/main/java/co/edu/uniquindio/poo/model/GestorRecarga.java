package co.edu.uniquindio.poo.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GestorRecarga {
    private final Map<String, SolicitudRecarga> solicitudes = new LinkedHashMap<>();

    public synchronized SolicitudRecarga solicitar(Cliente cliente, double monto) {
        SolicitudRecarga s = new SolicitudRecarga(cliente, monto);
        solicitudes.put(s.getId(), s);
        return s;
    }

    public synchronized List<SolicitudRecarga> listar() {
        return List.copyOf(solicitudes.values());
    }

    public synchronized SolicitudRecarga aprobar(String id) {
        SolicitudRecarga s = requerirPendiente(id);
        s.getCliente().getTarjeta().recargar(s.getMonto());
        s.cambiarEstado(EstadoSolicitudRecarga.APROBADA);
        return s;
    }

    public synchronized SolicitudRecarga rechazar(String id) {
        SolicitudRecarga s = requerirPendiente(id);
        s.cambiarEstado(EstadoSolicitudRecarga.RECHAZADA);
        return s;
    }

    private SolicitudRecarga requerirPendiente(String id) {
        SolicitudRecarga s = solicitudes.get(id);
        if (s == null || s.getEstado() != EstadoSolicitudRecarga.PENDIENTE)
            throw new IllegalStateException("La solicitud no existe o ya fue procesada.");
        return s;
    }
}
