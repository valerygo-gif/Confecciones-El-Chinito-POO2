package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.TipoPedido;
import java.time.LocalDate;
import java.util.List;

public record PedidoRequest(Long idCliente, LocalDate fechaEntregaComprometida, TipoPedido tipo,
                            String notas, List<PedidoDetalleRequest> detalles) { }
