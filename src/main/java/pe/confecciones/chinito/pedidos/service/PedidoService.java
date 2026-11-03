package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.pedidos.dto.PedidoRequest;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import java.util.List;

public interface PedidoService {
    PedidoResponse registrar(PedidoRequest request);
    PedidoResponse obtener(Long idPedido);
    List<PedidoResponse> listar(EstadoPedido estado);
    PedidoResponse cambiarEstado(Long idPedido, EstadoPedido nuevoEstado);
}
