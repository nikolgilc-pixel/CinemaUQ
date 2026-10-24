package co.edu.uniquindio.poo.model;

public class Pelicula {
    private final String id, titulo, clasificacion, sinopsis;
    private final GeneroPelicula genero;
    private final int duracionMinutos;
    private String rutaImagen;

    public Pelicula(String id, String titulo, GeneroPelicula genero, String clasificacion, int duracionMinutos, String sinopsis) {
        this.id = id;
        this.titulo = titulo;
        this.genero = java.util.Objects.requireNonNull(genero, "El género es obligatorio.");
        this.clasificacion = clasificacion;
        this.duracionMinutos = duracionMinutos;
        this.sinopsis = sinopsis;
    }

    public Pelicula(String id, String titulo, String genero, String clasificacion, int duracionMinutos, String sinopsis) {
        this(id, titulo, GeneroPelicula.valueOf(genero.trim().toUpperCase(java.util.Locale.ROOT)
                        .replace('Á','A').replace('É','E').replace('Í','I').replace('Ó','O').replace('Ú','U').replace(' ','_')),
                clasificacion, duracionMinutos, sinopsis);
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getGenero() {
        return genero.name().replace('_', ' ');
    }

    public GeneroPelicula getGeneroPelicula() { return genero; }

    public String getClasificacion() {
        return clasificacion;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public String getRutaImagen() { return rutaImagen; }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen == null || rutaImagen.isBlank() ? null : rutaImagen.trim();
    }

    @Override
    public String toString() {
        return titulo + " (" + clasificacion + ")";
    }
}
