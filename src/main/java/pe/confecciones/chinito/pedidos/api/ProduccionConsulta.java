package pe.confecciones.chinito.pedidos.api;

import pe.confecciones.chinito.pedidos.dto.OrdenProduccionResponse;
import java.util.List;

// Los otros módulos consultan el estado de producción de un pedido sin conocer nuestras clases internas.
public interface ProduccionConsulta {
    List<OrdenProduccionResponse> listarOrdenesDePedido(Long idPedido);
    boolean pedidoTieneOrdenesVigentes(Long idPedido);
}
