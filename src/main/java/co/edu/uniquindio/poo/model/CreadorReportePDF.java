package co.edu.uniquindio.poo.model;

public class CreadorReportePDF extends CreadorReporte {
    @Override
    public IReporte crearReporte() {
        return new ReportePDF(); }
}
