package co.edu.uniquindio.poo.model;

public class CreadorReporteTXT extends CreadorReporte {
    @Override
    public IReporte crearReporte() {
        return new ReporteTXT(); }
}
