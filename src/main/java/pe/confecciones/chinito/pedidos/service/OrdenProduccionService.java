package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.catalogo.api.CatalogoConsulta;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.api.ProduccionConsulta;
import pe.confecciones.chinito.pedidos.dto.*;
import pe.confecciones.chinito.pedidos.modelo.*;
import pe.confecciones.chinito.pedidos.repository.LineaOrdenRepository;
import pe.confecciones.chinito.pedidos.repository.OrdenProduccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrdenProduccionService implements ProduccionConsulta {
    private final OrdenProduccionRepository ordenes;
    private final LineaOrdenRepository lineas;
    private final PedidoConsulta pedidos;
    private final CatalogoConsulta catalogo;

    public OrdenProduccionService(OrdenProduccionRepository ordenes, LineaOrdenRepository lineas,
                                  PedidoConsulta pedidos, CatalogoConsulta catalogo) {
        this.ordenes = ordenes;
        this.lineas = lineas;
        this.pedidos = pedidos;
        this.catalogo = catalogo;
    }

    @Transactional
    public OrdenProduccionResponse crear(OrdenProduccionRequest r) {
        TipoOrden tipo = r.tipo() == null ? TipoOrden.PEDIDO : r.tipo();
        validarTipoYPedido(tipo, r.idPedido());
        if (tipo == TipoOrden.PEDIDO && !pedidos.existePedido(r.idPedido())) {
            throw new RecursoNoEncontradoException("Pedido", r.idPedido());
        }
        if (r.fechaObjetivo() == null) {
            throw new IllegalArgumentException("La fecha objetivo es obligatoria.");
        }
        if (r.prioridadManual() != null && (r.prioridadManual() < 1 || r.prioridadManual() > 5)) {
            throw new IllegalArgumentException("La prioridad manual debe estar entre 1 y 5.");
        }
        if (r.lineas() == null || r.lineas().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un detalle.");
        }
        OrdenProduccion o = new OrdenProduccion();
        o.setTipo(tipo);
        o.setIdPedido(r.idPedido());
        o.setFechaObjetivo(r.fechaObjetivo());
        o.setPrioridadManual(r.prioridadManual());
        o.setIndicaciones(r.indicaciones());
        OrdenProduccion guardada = ordenes.save(o);
        List<LineaOrden> nuevas = r.lineas().stream()
                .map(l -> construirLinea(guardada.getId(), l)).toList();
        return OrdenProduccionResponse.desde(guardada, lineas.saveAll(nuevas));
    }

    @Transactional(readOnly = true)
    public OrdenProduccionResponse obtener(Long idOrden) {
        return armarRespuesta(buscar(idOrden));
    }

    @Transactional(readOnly = true)
    public List<OrdenProduccionResponse> listar(EstadoOrden estado) {
        List<OrdenProduccion> lista = estado == null ? ordenes.findAllByOrderByFechaObjetivoAsc()
                : ordenes.findByEstadoOrderByFechaObjetivoAsc(estado);
        return lista.stream().map(this::armarRespuesta).toList();
    }

    @Transactional
    public OrdenProduccionResponse cambiarEstado(Long idOrden, EstadoOrden nuevoEstado) {
        OrdenProduccion o = buscar(idOrden);
        if (!o.getEstado().puedePasarA(nuevoEstado)) {
            throw new TransicionInvalidaException("orden de producción", o.getEstado(), nuevoEstado);
        }
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
        return ordenes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de producción", id));
    }

    private OrdenProduccionResponse armarRespuesta(OrdenProduccion o) {
        return OrdenProduccionResponse.desde(o, lineas.findByIdOrdenOrderByIdAsc(o.getId()));
    }

    private LineaOrden construirLinea(Long idOrden, OrdenProduccionRequest.Linea d) {
        if (d.idVariante() == null || catalogo.buscarVariante(d.idVariante()).isEmpty()) {
            throw new IllegalArgumentException("La variante indicada no existe en el catálogo.");
        }
        if (d.cantidadAProducir() == null || d.cantidadAProducir() <= 0) {
            throw new IllegalArgumentException("La cantidad a producir debe ser mayor a cero.");
        }
        LineaOrden linea = new LineaOrden();
        linea.setIdOrden(idOrden);
        linea.setIdVariante(d.idVariante());
        linea.setIdPedidoComponente(d.idPedidoComponente());
        linea.setCantidadAProducir(d.cantidadAProducir());
        return linea;
    }

    // Regla ck_op_pedido_o_stock: PEDIDO exige id_pedido; REPOSICION no puede tenerlo.
    private void validarTipoYPedido(TipoOrden tipo, Long idPedido) {
        if (tipo == TipoOrden.PEDIDO && idPedido == null) {
            throw new IllegalArgumentException("Una orden de tipo PEDIDO requiere idPedido.");
        }
        if (tipo == TipoOrden.REPOSICION && idPedido != null) {
            throw new IllegalArgumentException("Una orden de REPOSICION no debe tener idPedido.");
        }
    }
}
