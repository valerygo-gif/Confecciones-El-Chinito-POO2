package pe.confecciones.chinito.ventas.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.ventas.modelo.EstadoVenta;
import pe.confecciones.chinito.ventas.modelo.Venta;
import pe.confecciones.chinito.ventas.repository.VentaRepository;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;

// Pagos y entregas solo se registran sobre un pedido vigente con venta ACTIVA.
@Component
public class VentaActivaValidator {
    private final VentaRepository ventas;
    private final PedidoConsulta pedidos;

    public VentaActivaValidator(VentaRepository ventas, PedidoConsulta pedidos) {
        this.ventas = ventas;
        this.pedidos = pedidos;
    }

    public void validar(Long idPedido) {
        PedidoResponse pedido = pedidos.buscarPedido(idPedido)
                .orElseThrow(() -> new ReglaVentaException("El pedido " + idPedido + " no existe."));
        if (pedido.estado() == EstadoPedido.CANCELADO) {
            throw new ReglaVentaException("El pedido " + idPedido + " está cancelado.");
        }
        Venta venta = ventas.findByIdPedido(idPedido).orElseThrow(() ->
                new ReglaVentaException("El pedido " + idPedido + " no tiene venta registrada."));
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new ReglaVentaException("La venta del pedido " + idPedido + " está anulada.");
        }
    }
}
