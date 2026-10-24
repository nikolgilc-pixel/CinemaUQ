package co.edu.uniquindio.poo.model;

public enum TipoSala {
    DOS_D("2D"), TRES_D("3D"), CUATRO_D("4D"), IMAX("IMAX");
    private final String etiqueta;

    TipoSala(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
