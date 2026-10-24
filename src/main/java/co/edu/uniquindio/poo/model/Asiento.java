package co.edu.uniquindio.poo.model;

public class Asiento {
    private final String id, fila;
    private final int numero;
    private final TipoAsiento tipo;
    private Estado estado=Estado.DISPONIBLE;

    public Asiento(String id,String fila,int numero,TipoAsiento tipo){
        this.id=id;
        this.fila=fila;
        this.numero=numero;
        this.tipo=tipo;
    }

    public String getId(){
        return id;
    }

    public String getFila(){
        return fila;
    }

    public int getNumero(){
        return numero;
    }

    public TipoAsiento getTipo(){
        return tipo;
    }

    public Estado getEstado(){
        return estado;
    }

    public boolean estaDisponible(){
        return estado==Estado.DISPONIBLE;
    }

    public void ocupar(){ if(!estaDisponible())
        throw new AsientoNoDisponibleException("El asiento "+id+" ya está ocupado."); estado=Estado.OCUPADO; }
    public void liberar() { estado = Estado.DISPONIBLE; }

    @Override
    public String toString(){
        return fila+numero+" · "+tipo;
    }
}
