package pe.confecciones.chinito.ventas.dto;

import pe.confecciones.chinito.ventas.modelo.EstadoVenta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResponse(Long id, Long idPedido, String cliente, LocalDateTime fechaVenta, EstadoVenta estado,
                            BigDecimal total, BigDecimal pagado, BigDecimal saldo) {
}
