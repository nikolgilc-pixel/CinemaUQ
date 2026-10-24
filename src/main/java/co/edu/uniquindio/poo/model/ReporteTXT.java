package co.edu.uniquindio.poo.model;

public class ReporteTXT implements IReporte {
    @Override
    public String generar(String datos) {
        return datos == null ? "" : datos;
    }
}
