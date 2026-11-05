package pe.confecciones.chinito.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoDetalleResponse;
import pe.confecciones.chinito.ventas.dto.PagoRequest;
import pe.confecciones.chinito.ventas.modelo.EstadoPago;
import pe.confecciones.chinito.ventas.modelo.Pago;
import pe.confecciones.chinito.ventas.repository.PagoRepository;
import pe.confecciones.chinito.ventas.service.excepcion.PagoNoEncontradoException;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import pe.confecciones.chinito.ventas.service.validador.PagoAnulableValidator;
import pe.confecciones.chinito.ventas.service.validador.PagoRegistrableValidator;
import pe.confecciones.chinito.ventas.service.validador.VentaActivaValidator;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PagoServiceImpl implements PagoService {
    private final PagoRepository pagos;
    private final PedidoConsulta pedidos;
    private final VentaActivaValidator ventaActiva;
    private final PagoRegistrableValidator registrable;
    private final PagoAnulableValidator anulable;

    public PagoServiceImpl(PagoRepository pagos, PedidoConsulta pedidos, VentaActivaValidator ventaActiva,
                           PagoRegistrableValidator registrable, PagoAnulableValidator anulable) {
        this.pagos = pagos;
        this.pedidos = pedidos;
        this.ventaActiva = ventaActiva;
        this.registrable = registrable;
        this.anulable = anulable;
    }

    @Override
    public List<Pago> listar(Long idPedido) { return pagos.findByIdPedidoOrderByFechaPagoDesc(idPedido); }

    @Override
    public BigDecimal total(Long idPedido) {
        BigDecimal total = BigDecimal.ZERO;
        var pedido = pedidos.buscarPedido(idPedido)
                .orElseThrow(() -> new ReglaVentaException("El pedido " + idPedido + " no existe."));
        for (PedidoDetalleResponse d : pedido.detalles()) {
            total = total.add(d.precioUnitarioAcordado().multiply(BigDecimal.valueOf(d.cantidad())));
        }
        return total;
    }

    @Override
    public BigDecimal pagado(Long idPedido) {
        return listar(idPedido).stream().filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal saldo(Long idPedido) { return total(idPedido).subtract(pagado(idPedido)); }

    @Override
    @Transactional
    public Pago registrar(Long idPedido, PagoRequest datos) {
        ventaActiva.validar(idPedido);
        registrable.validar(datos, saldo(idPedido));
        Pago p = new Pago();
        p.setIdPedido(idPedido);
        p.setMonto(datos.monto());
        p.setMetodo(datos.metodo());
        p.setCuentaReceptora(datos.cuentaReceptora());
        p.setReferenciaOperacion(datos.referenciaOperacion());
        p.setObservaciones(datos.observaciones());
        return pagos.save(p);
    }

    @Override
    @Transactional
    public Pago anular(Long idPago) {
        Pago p = pagos.findById(idPago).orElseThrow(() -> new PagoNoEncontradoException(idPago));
        anulable.validar(p);
        p.setEstado(EstadoPago.ANULADO);
        return pagos.save(p);
    }
}
