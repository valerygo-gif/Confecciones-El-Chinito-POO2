package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.pedidos.dto.TrabajoRequest;
import pe.confecciones.chinito.pedidos.dto.TrabajoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoTrabajo;
import pe.confecciones.chinito.pedidos.modelo.EtapaProduccion;
import pe.confecciones.chinito.pedidos.modelo.OrdenProduccionDetalle;
import pe.confecciones.chinito.pedidos.modelo.TrabajoRealizado;
import pe.confecciones.chinito.pedidos.repository.OrdenProduccionDetalleRepository;
import pe.confecciones.chinito.pedidos.repository.TrabajoRealizadoRepository;
import pe.confecciones.chinito.pedidos.service.excepcion.OrdenProduccionNoEncontradaException;
import pe.confecciones.chinito.pedidos.service.excepcion.TrabajoNoEncontradoException;
import pe.confecciones.chinito.pedidos.service.validador.CantidadTerminadaValidator;
import pe.confecciones.chinito.pedidos.service.validador.TrabajoPendienteValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrabajoRealizadoServiceImpl implements TrabajoRealizadoService {
    private final TrabajoRealizadoRepository trabajos;
    private final OrdenProduccionDetalleRepository detalles;
    private final TrabajoPendienteValidator pendiente;
    private final CantidadTerminadaValidator terminada;

    public TrabajoRealizadoServiceImpl(TrabajoRealizadoRepository trabajos,
                                       OrdenProduccionDetalleRepository detalles,
                                       TrabajoPendienteValidator pendiente,
                                       CantidadTerminadaValidator terminada) {
        this.trabajos = trabajos;
        this.detalles = detalles;
        this.pendiente = pendiente;
        this.terminada = terminada;
    }

    @Override
    @Transactional
    public TrabajoResponse reportar(TrabajoRequest r) {
        if (r.idTrabajador() == null || r.etapa() == null) {
            throw new IllegalArgumentException("El trabajador y la etapa son obligatorios.");
        }
        if (r.cantidad() == null || r.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (r.tarifaPorUnidad() == null || r.tarifaPorUnidad().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tarifa por unidad no puede ser negativa.");
        }
        buscarDetalle(r.idOrdenDetalle());
        TrabajoRealizado t = new TrabajoRealizado();
        t.setIdOrdenDetalle(r.idOrdenDetalle());
        t.setIdTrabajador(r.idTrabajador());
        t.setEtapa(r.etapa());
        t.setFechaTrabajo(r.fechaTrabajo() == null ? LocalDate.now() : r.fechaTrabajo());
        t.setCantidad(r.cantidad());
        t.setTarifaPorUnidad(r.tarifaPorUnidad());
        t.setObservaciones(r.observaciones());
        return TrabajoResponse.desde(trabajos.save(t));
    }

    @Override
    @Transactional(readOnly = true)
    public TrabajoResponse obtener(Long idTrabajo) {
        return TrabajoResponse.desde(buscar(idTrabajo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrabajoResponse> listarPorOrdenDetalle(Long idOrdenDetalle) {
        return trabajos.findByIdOrdenDetalleOrderByFechaTrabajoDesc(idOrdenDetalle).stream()
                .map(TrabajoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrabajoResponse> listarPorTrabajador(Long idTrabajador) {
        return trabajos.findByIdTrabajadorOrderByFechaTrabajoDesc(idTrabajador).stream()
                .map(TrabajoResponse::desde).toList();
    }

    // Al validar un trabajo de ACABADO, esas unidades cuentan como prendas terminadas de la orden.
    @Override
    @Transactional
    public TrabajoResponse validar(Long idTrabajo) {
        TrabajoRealizado t = buscar(idTrabajo);
        pendiente.validar(t.getEstado(), EstadoTrabajo.VALIDADO);
        if (t.getEtapa() == EtapaProduccion.ACABADO) {
            OrdenProduccionDetalle d = buscarDetalle(t.getIdOrdenDetalle());
            int nuevoTotal = d.getCantidadTerminada() + t.getCantidad();
            terminada.validar(nuevoTotal, d.getCantidadAProducir());
            d.setCantidadTerminada(nuevoTotal);
            detalles.save(d);
        }
        t.setEstado(EstadoTrabajo.VALIDADO);
        t.setFechaValidacion(LocalDateTime.now());
        return TrabajoResponse.desde(trabajos.save(t));
    }

    @Override
    @Transactional
    public TrabajoResponse rechazar(Long idTrabajo, String observaciones) {
        TrabajoRealizado t = buscar(idTrabajo);
        pendiente.validar(t.getEstado(), EstadoTrabajo.RECHAZADO);
        t.setEstado(EstadoTrabajo.RECHAZADO);
        t.setFechaValidacion(LocalDateTime.now());
        if (observaciones != null && !observaciones.isBlank()) {
            t.setObservaciones(observaciones);
        }
        return TrabajoResponse.desde(trabajos.save(t));
    }

    private TrabajoRealizado buscar(Long id) {
        return trabajos.findById(id).orElseThrow(() -> new TrabajoNoEncontradoException(id));
    }

    private OrdenProduccionDetalle buscarDetalle(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El detalle de orden es obligatorio.");
        }
        return detalles.findById(id).orElseThrow(() ->
                new OrdenProduccionNoEncontradaException("Detalle de orden no encontrado: " + id));
    }
}
