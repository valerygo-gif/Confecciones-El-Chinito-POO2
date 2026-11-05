package pe.confecciones.chinito.ventas.service.excepcion;

public class ClienteNoEncontradoException extends ReglaVentaException {
    public ClienteNoEncontradoException(Long id) { super("Cliente no encontrado: " + id); }
}
