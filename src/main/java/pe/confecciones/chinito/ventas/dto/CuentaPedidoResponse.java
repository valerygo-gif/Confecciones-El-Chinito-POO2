package pe.confecciones.chinito.ventas.dto;

import pe.confecciones.chinito.ventas.modelo.Entrega;
import pe.confecciones.chinito.ventas.modelo.Pago;
import java.math.BigDecimal;
import java.util.List;

public record CuentaPedidoResponse(Long idPedido, BigDecimal total, BigDecimal pagado, BigDecimal saldo,
                                   List<Pago> pagos, List<Entrega> entregas) {
}
