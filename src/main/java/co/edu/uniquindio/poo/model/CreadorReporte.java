package co.edu.uniquindio.poo.model;

public abstract class CreadorReporte {

        public abstract IReporte crearReporte();

        public String exportar(String datos) {
        return crearReporte().generar(datos);
    }

}
