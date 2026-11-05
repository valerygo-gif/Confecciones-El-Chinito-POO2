package pe.confecciones.chinito.ventas.api;

import java.math.BigDecimal;

// Los otros módulos consultan el estado de cobro de un pedido solo a través de esta interfaz.
public interface VentaConsulta {
    boolean tieneVentaActiva(Long idPedido);
    BigDecimal saldoPendiente(Long idPedido);
}
