package pe.confecciones.chinito.pedidos.service.validador;

import pe.confecciones.chinito.pedidos.modelo.TipoOrden;
import org.springframework.stereotype.Component;

// Regla ck_op_pedido_o_stock: PEDIDO exige id_pedido; REPOSICION no puede tenerlo.
@Component
public class OrdenTipoPedidoValidator {
    public void validar(TipoOrden tipo, Long idPedido) {
        if (tipo == TipoOrden.PEDIDO && idPedido == null) {
            throw new IllegalArgumentException("Una orden de tipo PEDIDO requiere idPedido.");
        }
        if (tipo == TipoOrden.REPOSICION && idPedido != null) {
            throw new IllegalArgumentException("Una orden de REPOSICION no debe tener idPedido.");
        }
    }
}
