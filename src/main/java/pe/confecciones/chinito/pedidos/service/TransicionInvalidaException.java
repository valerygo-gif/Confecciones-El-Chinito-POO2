package pe.confecciones.chinito.pedidos.service;

// Se traduce a HTTP 409.
public class TransicionInvalidaException extends RuntimeException {
    public TransicionInvalidaException(String entidad, Object actual, Object nuevo) {
        super("Transición no permitida en " + entidad + ": " + actual + " -> " + nuevo);
    }
}
