package pe.confecciones.chinito.ventas.service.excepcion;

public class PagoNoEncontradoException extends ReglaVentaException {
    public PagoNoEncontradoException(Long id) { super("Pago no encontrado: " + id); }
}
