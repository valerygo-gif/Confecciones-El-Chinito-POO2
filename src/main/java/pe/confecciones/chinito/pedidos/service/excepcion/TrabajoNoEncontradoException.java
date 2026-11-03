package pe.confecciones.chinito.pedidos.service.excepcion;

public class TrabajoNoEncontradoException extends RuntimeException {
    public TrabajoNoEncontradoException(Long id) { super("Trabajo realizado no encontrado: " + id); }
}
