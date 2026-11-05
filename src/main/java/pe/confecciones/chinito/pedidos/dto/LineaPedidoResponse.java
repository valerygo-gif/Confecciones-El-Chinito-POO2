package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.LineaPedido;
import pe.confecciones.chinito.pedidos.modelo.TipoItemPedido;
import java.math.BigDecimal;

public record LineaPedidoResponse(Long id, TipoItemPedido tipoItem, Long idConjunto, String descripcion,
                                  Integer cantidad, BigDecimal precioUnitarioAcordado,
                                  String referenciaFotoUrl) {
    public static LineaPedidoResponse desde(LineaPedido l) {
        return new LineaPedidoResponse(l.getId(), l.getTipoItem(), l.getIdConjunto(), l.getDescripcion(),
                l.getCantidad(), l.getPrecioUnitarioAcordado(), l.getReferenciaFotoUrl());
    }
}
