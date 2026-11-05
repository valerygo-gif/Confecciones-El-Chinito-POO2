package pe.confecciones.chinito.ventas.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.ventas.repository.VentaRepository;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;

// Un pedido solo se vende si no está cancelado y no tiene ya una venta.
@Component
public class VentaRegistrableValidator {
    private final VentaRepository ventas;

    public VentaRegistrableValidator(VentaRepository ventas) { this.ventas = ventas; }

    public void validar(PedidoResponse pedido) {
        if (pedido.estado() == EstadoPedido.CANCELADO) {
            throw new ReglaVentaException("No se puede vender un pedido cancelado.");
        }
        if (ventas.findByIdPedido(pedido.id()).isPresent()) {
            throw new ReglaVentaException("El pedido " + pedido.id() + " ya tiene una venta registrada.");
        }
    }
}
