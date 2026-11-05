package pe.confecciones.chinito.ventas.service;

import pe.confecciones.chinito.ventas.dto.PagoRequest;
import pe.confecciones.chinito.ventas.modelo.Pago;
import java.math.BigDecimal;
import java.util.List;

public interface PagoService {
    List<Pago> listar(Long idPedido);
    // Total acordado: suma de cantidad x precio de cada línea del pedido.
    BigDecimal total(Long idPedido);
    // Suma de los pagos confirmados (los anulados no cuentan).
    BigDecimal pagado(Long idPedido);
    BigDecimal saldo(Long idPedido);
    Pago registrar(Long idPedido, PagoRequest datos);
    Pago anular(Long idPago);
}
