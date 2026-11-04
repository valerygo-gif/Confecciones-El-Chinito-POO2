package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.PedidoDetalle;
import pe.confecciones.chinito.pedidos.modelo.TipoItemPedido;
import java.math.BigDecimal;

public record PedidoDetalleResponse(Long id, TipoItemPedido tipoItem, Long idConjunto, String descripcion,
                                    Integer cantidad, BigDecimal precioUnitarioAcordado,
                                    String referenciaFotoUrl) {
    public static PedidoDetalleResponse desde(PedidoDetalle d) {
        return new PedidoDetalleResponse(d.getId(), d.getTipoItem(), d.getIdConjunto(), d.getDescripcion(),
                d.getCantidad(), d.getPrecioUnitarioAcordado(), d.getReferenciaFotoUrl());
    }
}
