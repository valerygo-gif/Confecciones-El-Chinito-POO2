package pe.confecciones.chinito.compras.service.excepcion;

public class CompraNoEncontradaException extends RuntimeException {
    public CompraNoEncontradaException(Long id) { super("La compra " + id + " no existe."); }
}
