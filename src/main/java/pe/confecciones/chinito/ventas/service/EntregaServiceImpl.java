package pe.confecciones.chinito.ventas.service;

import org.springframework.stereotype.Service;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.ventas.dto.EntregaRequest;
import pe.confecciones.chinito.ventas.modelo.Entrega;
import pe.confecciones.chinito.ventas.repository.EntregaRepository;
import pe.confecciones.chinito.ventas.service.validador.VentaActivaValidator;
import java.util.List;

@Service
public class EntregaServiceImpl implements EntregaService {
    private final EntregaRepository entregas;
    private final VentaActivaValidator ventaActiva;
    private final PedidoConsulta pedidos;

    public EntregaServiceImpl(EntregaRepository entregas, VentaActivaValidator ventaActiva, PedidoConsulta pedidos) {
        this.entregas = entregas;
        this.ventaActiva = ventaActiva;
        this.pedidos = pedidos;
    }

    @Override
    public List<Entrega> listar(Long idPedido) { return entregas.findByIdPedidoOrderByFechaEntregaDesc(idPedido); }

    @Override
    @Transactional
    public Entrega registrar(Long idPedido, EntregaRequest datos) {
        ventaActiva.validar(idPedido);
        // Solo se entrega lo que ya está terminado: el pedido debe estar LISTO o con una entrega parcial previa.
        EstadoPedido estado = pedidos.buscarPedido(idPedido).orElseThrow(
                () -> new ReglaVentaException("El pedido " + idPedido + " no existe.")).estado();
        if (estado != EstadoPedido.LISTO && estado != EstadoPedido.ENTREGA_PARCIAL) {
            throw new ReglaVentaException("El pedido " + idPedido + " está " + estado
                    + ": solo se puede entregar un pedido LISTO o con entrega parcial.");
        }
        Entrega e = new Entrega();
        e.setIdPedido(idPedido);
        e.setObservaciones(datos.observaciones());
        return entregas.save(e);
    }
}
