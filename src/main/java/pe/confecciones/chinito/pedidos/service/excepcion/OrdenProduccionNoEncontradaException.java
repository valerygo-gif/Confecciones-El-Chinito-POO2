package pe.confecciones.chinito.pedidos.service.excepcion;

public class OrdenProduccionNoEncontradaException extends RuntimeException {
    public OrdenProduccionNoEncontradaException(Long id) { super("Orden de producción no encontrada: " + id); }
    public OrdenProduccionNoEncontradaException(String mensaje) { super(mensaje); }
}
