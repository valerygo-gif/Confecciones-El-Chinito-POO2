package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.*;
import pe.confecciones.chinito.pedidos.modelo.*;
import pe.confecciones.chinito.pedidos.repository.PedidoDetalleRepository;
import pe.confecciones.chinito.pedidos.repository.PedidoRepository;
import pe.confecciones.chinito.pedidos.service.excepcion.PedidoNoEncontradoException;
import pe.confecciones.chinito.pedidos.service.validador.PedidoDetalleConjuntoValidator;
import pe.confecciones.chinito.pedidos.service.validador.TransicionEstadoPedidoValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoServiceImpl implements PedidoService, PedidoConsulta {
    private final PedidoRepository pedidos;
    private final PedidoDetalleRepository detalles;
    private final TransicionEstadoPedidoValidator transicion;
    private final PedidoDetalleConjuntoValidator conjunto;

    public PedidoServiceImpl(PedidoRepository pedidos, PedidoDetalleRepository detalles,
                             TransicionEstadoPedidoValidator transicion,
                             PedidoDetalleConjuntoValidator conjunto) {
        this.pedidos = pedidos;
        this.detalles = detalles;
        this.transicion = transicion;
        this.conjunto = conjunto;
    }

    @Override
    @Transactional
    public PedidoResponse registrar(PedidoRequest r) {
        if (r.idCliente() == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        if (r.detalles() == null || r.detalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un detalle.");
        }
        Pedido p = new Pedido();
        p.setIdCliente(r.idCliente());
        p.setFechaEntregaComprometida(r.fechaEntregaComprometida());
        p.setTipo(r.tipo() == null ? TipoPedido.REGULAR : r.tipo());
        p.setNotas(r.notas());
        Pedido guardado = pedidos.save(p);
        List<PedidoDetalle> lineas = r.detalles().stream()
                .map(d -> construirDetalle(guardado.getId(), d)).toList();
        return PedidoResponse.desde(guardado, detalles.saveAll(lineas));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long idPedido) {
        return armarRespuesta(buscar(idPedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(EstadoPedido estado) {
        List<Pedido> lista = estado == null ? pedidos.findAllByOrderByFechaPedidoDesc()
                : pedidos.findByEstadoOrderByFechaEntregaComprometidaAsc(estado);
        return lista.stream().map(this::armarRespuesta).toList();
    }

    @Override
    @Transactional
    public PedidoResponse cambiarEstado(Long idPedido, EstadoPedido nuevoEstado) {
        Pedido p = buscar(idPedido);
        transicion.validar(p.getEstado(), nuevoEstado);
        p.setEstado(nuevoEstado);
        return armarRespuesta(pedidos.save(p));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PedidoResponse> buscarPedido(Long idPedido) {
        return pedidos.findById(idPedido).map(this::armarRespuesta);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePedido(Long idPedido) {
        return idPedido != null && pedidos.existsById(idPedido);
    }

    private Pedido buscar(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
    }

    private PedidoResponse armarRespuesta(Pedido p) {
        return PedidoResponse.desde(p, detalles.findByIdPedidoOrderByIdAsc(p.getId()));
    }

    private PedidoDetalle construirDetalle(Long idPedido, PedidoDetalleRequest d) {
        conjunto.validar(d.tipoItem(), d.idConjunto());
        if (d.descripcion() == null || d.descripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción del detalle es obligatoria.");
        }
        if (d.cantidad() == null || d.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad del detalle debe ser mayor a cero.");
        }
        if (d.precioUnitarioAcordado() == null || d.precioUnitarioAcordado().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio acordado no puede ser negativo.");
        }
        PedidoDetalle linea = new PedidoDetalle();
        linea.setIdPedido(idPedido);
        linea.setTipoItem(d.tipoItem());
        linea.setIdConjunto(d.idConjunto());
        linea.setDescripcion(d.descripcion().trim());
        linea.setCantidad(d.cantidad());
        linea.setPrecioUnitarioAcordado(d.precioUnitarioAcordado());
        linea.setReferenciaFotoUrl(d.referenciaFotoUrl());
        return linea;
    }
}
