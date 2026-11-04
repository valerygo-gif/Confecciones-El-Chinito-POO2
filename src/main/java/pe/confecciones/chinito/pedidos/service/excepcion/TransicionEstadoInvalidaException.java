package pe.confecciones.chinito.pedidos.service.excepcion;

public class TransicionEstadoInvalidaException extends RuntimeException {
    public TransicionEstadoInvalidaException(String entidad, Object actual, Object nuevo) {
        super("Transición no permitida en " + entidad + ": " + actual + " -> " + nuevo);
    }
}
