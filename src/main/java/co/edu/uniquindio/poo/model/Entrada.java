package co.edu.uniquindio.poo.model;

public class Entrada {
    private final String id;
    private final Funcion funcion;
    private final Asiento asiento;
    private final double valor;
    private boolean asistida;

    public Entrada(String id, Funcion funcion, Asiento asiento) {
        this.id = id;
        this.funcion = funcion;
        this.asiento = asiento;
        valor = funcion.calcularPrecio(asiento);
    }

    public String getId() {
        return id;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public Asiento getAsiento() {
        return asiento;
    }

    public double getValor() {
        return valor;
    }

    public boolean isAsistida() {
        return asistida;
    }

    void marcarAsistida() {
        if (asistida) throw new IllegalStateException("La asistencia ya fue registrada.");
        asistida = true;
    }

    @Override
    public String toString() {
        return funcion.getPelicula().getTitulo() + " · Asiento " + asiento.getFila() + asiento.getNumero() + (asistida ? " · asistencia registrada" : "");
    }
}
