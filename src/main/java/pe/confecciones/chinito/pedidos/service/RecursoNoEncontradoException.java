package pe.confecciones.chinito.pedidos.service;

// Se traduce a HTTP 404.
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " no encontrado: " + id);
    }
}
