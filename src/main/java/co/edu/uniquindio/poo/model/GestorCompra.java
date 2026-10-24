package co.edu.uniquindio.poo.model;

import java.util.*;

public class GestorCompra {
    private final IPagoTarjeta pago;
    private final List<Compra> compras=new ArrayList<>();

    public GestorCompra(IPagoTarjeta pago){

        this.pago=pago;
    }

    public synchronized Compra registrarCompra(Compra compra, List<Asiento> seleccion, Map<Producto,Integer> productos, Map<IComponenteConfiteria,Integer> combos){

        if(compra==null||compra.getCliente()==null||compra.getFuncion()==null||seleccion==null||seleccion.isEmpty())
            throw new CompraInvalidaException("Selecciona función y al menos un asiento.");

        if(new HashSet<>(seleccion).size()!=seleccion.size())
            throw new CompraInvalidaException("Hay asientos repetidos.");
        for(Asiento a:seleccion)if(!a.estaDisponible()||!compra.getFuncion().getAsientos().contains(a))
            throw new AsientoNoDisponibleException("El asiento "+a+" ya no está disponible.");
        Map<Producto,Integer> consumo=new LinkedHashMap<>(productos);
        combos.forEach((c,n)->acumularProductos(c,n,consumo));
        consumo.forEach((p,n)->{if(n<1||p.getStock()<n)
            throw new StockInsuficienteException("Stock insuficiente para "+p.getNombre());});
        double total=0;
        for(Asiento a:seleccion){Entrada e=new Entrada(UUID.randomUUID().toString(),compra.getFuncion(),a);compra.agregarEntrada(e);total+=e.getValor();}
        productos.forEach((p,n)->compra.agregarDetalle(
                new DetalleCompra(p.getNombre(),n,p.calcularPrecio())));
        combos.forEach((c,n)->compra.agregarDetalle(
                new DetalleCompra(c.getNombre(),n,c.calcularPrecio())));
        total+=productos.entrySet().stream().mapToDouble(e->e.getKey().calcularPrecio()*e.getValue()).sum();
        total+=combos.entrySet().stream().mapToDouble(e->e.getKey().calcularPrecio()*e.getValue()).sum();
        pago.procesarPago(compra.getCliente().getTarjeta(),total);
        seleccion.forEach(Asiento::ocupar); consumo.forEach(Producto::descontarStock);
        compra.confirmar(total);compra.getCliente().getRegistroPuntos().acumular(total);compras.add(compra);
        return compra;
    }

    private void acumularProductos(IComponenteConfiteria c,int cantidad,Map<Producto,Integer> consumo){
        if(c instanceof Producto p)consumo.merge(p,cantidad,Integer::sum);
        else if(c instanceof Combo combo)for(IComponenteConfiteria hijo:combo.getComponentes())acumularProductos(hijo,cantidad,consumo);
        else if(c instanceof ComboConPromocion decorador)acumularProductos(decorador.getComponente(),cantidad,consumo);
    }

    public List<Compra> consultarCompras(){return List.copyOf(compras);}

}
