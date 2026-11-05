package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.*;
import pe.confecciones.chinito.pedidos.modelo.*;
import pe.confecciones.chinito.pedidos.repository.LineaPedidoRepository;
import pe.confecciones.chinito.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService implements PedidoConsulta {
    private final PedidoRepository pedidos;
    private final LineaPedidoRepository lineas;

    public PedidoService(PedidoRepository pedidos, LineaPedidoRepository lineas) {
        this.pedidos = pedidos;
        this.lineas = lineas;
    }

    @Transactional
    public PedidoResponse registrar(PedidoRequest r) {
        if (r.idCliente() == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        if (r.lineas() == null || r.lineas().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un detalle.");
        }
        Pedido p = new Pedido();
        p.setIdCliente(r.idCliente());
        p.setFechaEntregaComprometida(r.fechaEntregaComprometida());
        p.setTipo(r.tipo() == null ? TipoPedido.REGULAR : r.tipo());
        p.setNotas(r.notas());
        Pedido guardado = pedidos.save(p);
        List<LineaPedido> nuevas = r.lineas().stream()
                .map(l -> construirLinea(guardado.getId(), l)).toList();
        return PedidoResponse.desde(guardado, lineas.saveAll(nuevas));
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long idPedido) {
        return armarRespuesta(buscar(idPedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(EstadoPedido estado) {
        List<Pedido> lista = estado == null ? pedidos.findAllByOrderByFechaPedidoDesc()
                : pedidos.findByEstadoOrderByFechaEntregaComprometidaAsc(estado);
        return lista.stream().map(this::armarRespuesta).toList();
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long idPedido, EstadoPedido nuevoEstado) {
        Pedido p = buscar(idPedido);
        if (!p.getEstado().puedePasarA(nuevoEstado)) {
            throw new TransicionInvalidaException("pedido", p.getEstado(), nuevoEstado);
        }
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
        return pedidos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
    }

    private PedidoResponse armarRespuesta(Pedido p) {
        return PedidoResponse.desde(p, lineas.findByIdPedidoOrderByIdAsc(p.getId()));
    }

    private LineaPedido construirLinea(Long idPedido, PedidoRequest.Linea d) {
        validarConjunto(d.tipoItem(), d.idConjunto());
        if (d.descripcion() == null || d.descripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción del detalle es obligatoria.");
        }
        if (d.cantidad() == null || d.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad del detalle debe ser mayor a cero.");
        }
        if (d.precioUnitarioAcordado() == null || d.precioUnitarioAcordado().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio acordado no puede ser negativo.");
        }
        LineaPedido linea = new LineaPedido();
        linea.setIdPedido(idPedido);
        linea.setTipoItem(d.tipoItem());
        linea.setIdConjunto(d.idConjunto());
        linea.setDescripcion(d.descripcion().trim());
        linea.setCantidad(d.cantidad());
        linea.setPrecioUnitarioAcordado(d.precioUnitarioAcordado());
        linea.setReferenciaFotoUrl(d.referenciaFotoUrl());
        return linea;
    }

    // Regla ck_pd_conjunto: CONJUNTO exige id_conjunto; los demás tipos deben dejarlo nulo.
    private void validarConjunto(TipoItemPedido tipoItem, Long idConjunto) {
        if (tipoItem == null) {
            throw new IllegalArgumentException("El tipo de ítem es obligatorio.");
        }
        boolean esConjunto = tipoItem == TipoItemPedido.CONJUNTO;
        if (esConjunto && idConjunto == null) {
            throw new IllegalArgumentException("Un ítem de tipo CONJUNTO requiere idConjunto.");
        }
        if (!esConjunto && idConjunto != null) {
            throw new IllegalArgumentException("Solo un ítem de tipo CONJUNTO puede indicar idConjunto.");
        }
    }
}
