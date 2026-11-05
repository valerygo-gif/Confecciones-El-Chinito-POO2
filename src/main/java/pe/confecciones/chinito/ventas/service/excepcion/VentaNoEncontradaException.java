package pe.confecciones.chinito.ventas.service.excepcion;

public class VentaNoEncontradaException extends ReglaVentaException {
    public VentaNoEncontradaException(Long id) { super("Venta no encontrada: " + id); }
}
