package pe.confecciones.chinito.pedidos.service.excepcion;

public class PedidoNoEncontradoException extends RuntimeException {
    public PedidoNoEncontradoException(Long id) { super("Pedido no encontrado: " + id); }
}
