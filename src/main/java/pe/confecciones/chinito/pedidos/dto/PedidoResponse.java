package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.modelo.Pedido;
import pe.confecciones.chinito.pedidos.modelo.PedidoDetalle;
import pe.confecciones.chinito.pedidos.modelo.TipoPedido;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, Long idCliente, LocalDateTime fechaPedido,
                             LocalDate fechaEntregaComprometida, TipoPedido tipo, EstadoPedido estado,
                             String notas, List<PedidoDetalleResponse> detalles) {
    public static PedidoResponse desde(Pedido p, List<PedidoDetalle> lineas) {
        return new PedidoResponse(p.getId(), p.getIdCliente(), p.getFechaPedido(),
                p.getFechaEntregaComprometida(), p.getTipo(), p.getEstado(), p.getNotas(),
                lineas.stream().map(PedidoDetalleResponse::desde).toList());
    }
}
