package pe.confecciones.chinito.ventas.service;

import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoDetalleResponse;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.ventas.modelo.*;
import pe.confecciones.chinito.ventas.repository.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

// Ventas, pagos y entregas de un pedido. Consulta pedidos solo mediante PedidoConsulta.
@Service
public class VentaService {
    private final VentaRepository ventas;
    private final PagoRepository pagos;
    private final EntregaRepository entregas;
    private final PedidoConsulta pedidos;

    public VentaService(VentaRepository ventas, PagoRepository pagos, EntregaRepository entregas,
                        PedidoConsulta pedidos) {
        this.ventas = ventas;
        this.pagos = pagos;
        this.entregas = entregas;
        this.pedidos = pedidos;
    }

    public List<Venta> listarVentas() { return ventas.findAllByOrderByFechaVentaDesc(); }
    public List<Pago> listarPagos(Long idPedido) { return pagos.findByIdPedidoOrderByFechaPagoDesc(idPedido); }
    public List<Entrega> listarEntregas(Long idPedido) { return entregas.findByIdPedidoOrderByFechaEntregaDesc(idPedido); }

    public Venta registrarVenta(Long idPedido) {
        PedidoResponse p = pedido(idPedido);
        if (p.estado() == pe.confecciones.chinito.pedidos.modelo.EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException("No se puede vender un pedido cancelado.");
        }
        if (ventas.findByIdPedido(idPedido).isPresent()) {
            throw new IllegalArgumentException("El pedido " + idPedido + " ya tiene una venta registrada.");
        }
        Venta v = new Venta();
        v.setIdPedido(idPedido);
        return ventas.save(v);
    }

    public void anularVenta(Long idVenta) {
        Venta v = ventas.findById(idVenta).orElseThrow(() -> new IllegalArgumentException("Venta no encontrada."));
        v.setEstado(Venta.Estado.ANULADA);
        ventas.save(v);
    }

    // Total acordado del pedido: suma de cantidad x precio de cada línea.
    public BigDecimal total(Long idPedido) {
        BigDecimal total = BigDecimal.ZERO;
        for (PedidoDetalleResponse d : pedido(idPedido).detalles()) {
            total = total.add(d.precioUnitarioAcordado().multiply(BigDecimal.valueOf(d.cantidad())));
        }
        return total;
    }

    // Suma de los pagos confirmados (los anulados no cuentan).
    public BigDecimal pagado(Long idPedido) {
        return listarPagos(idPedido).stream().filter(x -> x.getEstado() == Pago.Estado.CONFIRMADO)
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Pago registrarPago(Long idPedido, Pago p) {
        if (p.getMonto() == null || p.getMonto().signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        BigDecimal saldo = total(idPedido).subtract(pagado(idPedido));
        if (p.getMonto().compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El monto (" + p.getMonto() + ") supera el saldo pendiente (" + saldo + ").");
        }
        p.setIdPedido(idPedido);
        return pagos.save(p);
    }

    public void anularPago(Long idPago) {
        Pago p = pagos.findById(idPago).orElseThrow(() -> new IllegalArgumentException("Pago no encontrado."));
        p.setEstado(Pago.Estado.ANULADO);
        pagos.save(p);
    }

    public Entrega registrarEntrega(Long idPedido, String observaciones) {
        pedido(idPedido);
        Entrega e = new Entrega();
        e.setIdPedido(idPedido);
        e.setObservaciones(observaciones);
        return entregas.save(e);
    }

    private PedidoResponse pedido(Long idPedido) {
        return pedidos.buscarPedido(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("El pedido " + idPedido + " no existe."));
    }
}
