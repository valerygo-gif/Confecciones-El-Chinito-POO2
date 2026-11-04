package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.TipoItemPedido;
import java.math.BigDecimal;

public record PedidoDetalleRequest(TipoItemPedido tipoItem, Long idConjunto, String descripcion,
                                   Integer cantidad, BigDecimal precioUnitarioAcordado,
                                   String referenciaFotoUrl) { }
