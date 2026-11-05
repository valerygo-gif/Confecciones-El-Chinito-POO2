package pe.confecciones.chinito.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.ventas.api.ClienteConsulta;
import pe.confecciones.chinito.ventas.api.VentaConsulta;
import pe.confecciones.chinito.ventas.dto.CuentaPedidoResponse;
import pe.confecciones.chinito.ventas.dto.VentaResponse;
import pe.confecciones.chinito.ventas.modelo.EstadoVenta;
import pe.confecciones.chinito.ventas.modelo.Venta;
import pe.confecciones.chinito.ventas.repository.VentaRepository;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import pe.confecciones.chinito.ventas.service.excepcion.VentaNoEncontradaException;
import pe.confecciones.chinito.ventas.service.validador.VentaAnulableValidator;
import pe.confecciones.chinito.ventas.service.validador.VentaRegistrableValidator;
import java.math.BigDecimal;
import java.util.List;

// Ventas de un pedido. Consulta pedidos solo mediante PedidoConsulta y clientes mediante ClienteConsulta.
@Service
public class VentaServiceImpl implements VentaService, VentaConsulta {
    private final VentaRepository ventas;
    private final PedidoConsulta pedidos;
    private final ClienteConsulta clientes;
    private final PagoService pagos;
    private final EntregaService entregas;
    private final VentaRegistrableValidator registrable;
    private final VentaAnulableValidator anulable;

    public VentaServiceImpl(VentaRepository ventas, PedidoConsulta pedidos, ClienteConsulta clientes,
                            PagoService pagos, EntregaService entregas,
                            VentaRegistrableValidator registrable, VentaAnulableValidator anulable) {
        this.ventas = ventas;
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.pagos = pagos;
        this.entregas = entregas;
        this.registrable = registrable;
        this.anulable = anulable;
    }

    @Override
    public List<VentaResponse> listar() {
        return ventas.findAllByOrderByFechaVentaDesc().stream().map(this::respuesta).toList();
    }

    @Override
    @Transactional
    public Venta registrar(Long idPedido) {
        PedidoResponse pedido = pedidos.buscarPedido(idPedido)
                .orElseThrow(() -> new ReglaVentaException("El pedido " + idPedido + " no existe."));
        registrable.validar(pedido);
        Venta v = new Venta();
        v.setIdPedido(idPedido);
        return ventas.save(v);
    }

    @Override
    @Transactional
    public void anular(Long idVenta) {
        Venta v = ventas.findById(idVenta).orElseThrow(() -> new VentaNoEncontradaException(idVenta));
        anulable.validar(v, pagos.pagado(v.getIdPedido()));
        v.setEstado(EstadoVenta.ANULADA);
        ventas.save(v);
    }

    @Override
    public CuentaPedidoResponse cuenta(Long idPedido) {
        BigDecimal total = pagos.total(idPedido);
        BigDecimal pagado = pagos.pagado(idPedido);
        return new CuentaPedidoResponse(idPedido, total, pagado, total.subtract(pagado),
                pagos.listar(idPedido), entregas.listar(idPedido));
    }

    @Override
    public boolean tieneVentaActiva(Long idPedido) {
        return ventas.findByIdPedido(idPedido).filter(v -> v.getEstado() == EstadoVenta.ACTIVA).isPresent();
    }

    @Override
    public BigDecimal saldoPendiente(Long idPedido) { return pagos.saldo(idPedido); }

    private VentaResponse respuesta(Venta v) {
        Long idPedido = v.getIdPedido();
        BigDecimal total = pagos.total(idPedido);
        BigDecimal pagado = pagos.pagado(idPedido);
        String cliente = pedidos.buscarPedido(idPedido)
                .flatMap(p -> clientes.nombreCliente(p.idCliente())).orElse("-");
        return new VentaResponse(v.getId(), idPedido, cliente, v.getFechaVenta(), v.getEstado(),
                total, pagado, total.subtract(pagado));
    }
}
