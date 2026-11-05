package pe.confecciones.chinito.ventas.service;

import pe.confecciones.chinito.ventas.dto.CuentaPedidoResponse;
import pe.confecciones.chinito.ventas.dto.VentaResponse;
import pe.confecciones.chinito.ventas.modelo.Venta;
import java.util.List;

public interface VentaService {
    List<VentaResponse> listar();
    Venta registrar(Long idPedido);
    void anular(Long idVenta);
    // Total, pagos y entregas de un pedido.
    CuentaPedidoResponse cuenta(Long idPedido);
}
