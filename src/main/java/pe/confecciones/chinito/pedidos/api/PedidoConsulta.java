package pe.confecciones.chinito.pedidos.api;

import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import java.util.Optional;

// Los otros módulos (ventas, compras) consultan pedidos solo a través de esta interfaz.
public interface PedidoConsulta {
    Optional<PedidoResponse> buscarPedido(Long idPedido);
    boolean existePedido(Long idPedido);
}
