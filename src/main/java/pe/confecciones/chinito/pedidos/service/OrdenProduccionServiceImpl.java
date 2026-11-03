package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.catalogo.api.CatalogoConsulta;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.api.ProduccionConsulta;
import pe.confecciones.chinito.pedidos.dto.*;
import pe.confecciones.chinito.pedidos.modelo.*;
import pe.confecciones.chinito.pedidos.repository.OrdenProduccionDetalleRepository;
import pe.confecciones.chinito.pedidos.repository.OrdenProduccionRepository;
import pe.confecciones.chinito.pedidos.service.excepcion.OrdenProduccionNoEncontradaException;
import pe.confecciones.chinito.pedidos.service.excepcion.PedidoNoEncontradoException;
import pe.confecciones.chinito.pedidos.service.validador.OrdenTipoPedidoValidator;
import pe.confecciones.chinito.pedidos.service.validador.TransicionEstadoOrdenValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrdenProduccionServiceImpl implements OrdenProduccionService, ProduccionConsulta {
    private final OrdenProduccionRepository ordenes;
    private final OrdenProduccionDetalleRepository detalles;
    private final PedidoConsulta pedidos;
    private final CatalogoConsulta catalogo;
    private final TransicionEstadoOrdenValidator transicion;
    private final OrdenTipoPedidoValidator tipoPedido;

    public OrdenProduccionServiceImpl(OrdenProduccionRepository ordenes,
                                      OrdenProduccionDetalleRepository detalles,
                                      PedidoConsulta pedidos, CatalogoConsulta catalogo,
                                      TransicionEstadoOrdenValidator transicion,
                                      OrdenTipoPedidoValidator tipoPedido) {
        this.ordenes = ordenes;
        this.detalles = detalles;
        this.pedidos = pedidos;
        this.catalogo = catalogo;
        this.transicion = transicion;
        this.tipoPedido = tipoPedido;
    }

    @Override
    @Transactional
    public OrdenProduccionResponse crear(OrdenProduccionRequest r) {
        TipoOrden tipo = r.tipo() == null ? TipoOrden.PEDIDO : r.tipo();
        tipoPedido.validar(tipo, r.idPedido());
        if (tipo == TipoOrden.PEDIDO && !pedidos.existePedido(r.idPedido())) {
            throw new PedidoNoEncontradoException(r.idPedido());
        }
        if (r.fechaObjetivo() == null) {
            throw new IllegalArgumentException("La fecha objetivo es obligatoria.");
        }
        if (r.prioridadManual() != null && (r.prioridadManual() < 1 || r.prioridadManual() > 5)) {
            throw new IllegalArgumentException("La prioridad manual debe estar entre 1 y 5.");
        }
        if (r.detalles() == null || r.detalles().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un detalle.");
        }
        OrdenProduccion o = new OrdenProduccion();
        o.setTipo(tipo);
        o.setIdPedido(r.idPedido());
        o.setFechaObjetivo(r.fechaObjetivo());
        o.setPrioridadManual(r.prioridadManual());
        o.setIndicaciones(r.indicaciones());
        OrdenProduccion guardada = ordenes.save(o);
        List<OrdenProduccionDetalle> lineas = r.detalles().stream()
                .map(d -> construirDetalle(guardada.getId(), d)).toList();
        return OrdenProduccionResponse.desde(guardada, detalles.saveAll(lineas));
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenProduccionResponse obtener(Long idOrden) {
        return armarRespuesta(buscar(idOrden));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenProduccionResponse> listar(EstadoOrden estado) {
        List<OrdenProduccion> lista = estado == null ? ordenes.findAllByOrderByFechaObjetivoAsc()
                : ordenes.findByEstadoOrderByFechaObjetivoAsc(estado);
        return lista.stream().map(this::armarRespuesta).toList();
    }

    @Override
    @Transactional
    public OrdenProduccionResponse cambiarEstado(Long idOrden, EstadoOrden nuevoEstado) {
        OrdenProduccion o = buscar(idOrden);
        transicion.validar(o.getEstado(), nuevoEstado);
        o.setEstado(nuevoEstado);
        return armarRespuesta(ordenes.save(o));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenProduccionResponse> listarOrdenesDePedido(Long idPedido) {
        return ordenes.findByIdPedidoOrderByFechaCreacionAsc(idPedido).stream()
                .map(this::armarRespuesta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean pedidoTieneOrdenesVigentes(Long idPedido) {
        return ordenes.existsByIdPedidoAndEstadoNot(idPedido, EstadoOrden.CANCELADA);
    }

    private OrdenProduccion buscar(Long id) {
        return ordenes.findById(id).orElseThrow(() -> new OrdenProduccionNoEncontradaException(id));
    }

    private OrdenProduccionResponse armarRespuesta(OrdenProduccion o) {
        return OrdenProduccionResponse.desde(o, detalles.findByIdOrdenOrderByIdAsc(o.getId()));
    }

    private OrdenProduccionDetalle construirDetalle(Long idOrden, OrdenDetalleRequest d) {
        if (d.idVariante() == null || catalogo.buscarVariante(d.idVariante()).isEmpty()) {
            throw new IllegalArgumentException("La variante indicada no existe en el catálogo.");
        }
        if (d.cantidadAProducir() == null || d.cantidadAProducir() <= 0) {
            throw new IllegalArgumentException("La cantidad a producir debe ser mayor a cero.");
        }
        OrdenProduccionDetalle linea = new OrdenProduccionDetalle();
        linea.setIdOrden(idOrden);
        linea.setIdVariante(d.idVariante());
        linea.setIdPedidoComponente(d.idPedidoComponente());
        linea.setCantidadAProducir(d.cantidadAProducir());
        return linea;
    }
}
