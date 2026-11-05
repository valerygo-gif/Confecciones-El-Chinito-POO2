package pe.confecciones.chinito.compras.service.excepcion;

public class ProveedorNoEncontradoException extends RuntimeException {
    public ProveedorNoEncontradoException(Long id) { super("El proveedor " + id + " no existe."); }
}
