package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.pedidos.dto.*;
import pe.confecciones.chinito.pedidos.modelo.*;
import pe.confecciones.chinito.pedidos.repository.LineaOrdenRepository;
import pe.confecciones.chinito.pedidos.repository.TrabajadorRepository;
import pe.confecciones.chinito.pedidos.repository.TrabajoRealizadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrabajoRealizadoService {
    private final TrabajoRealizadoRepository trabajos;
    private final LineaOrdenRepository lineasOrden;
    private final TrabajadorRepository trabajadores;

    public TrabajoRealizadoService(TrabajoRealizadoRepository trabajos, LineaOrdenRepository lineasOrden,
                                   TrabajadorRepository trabajadores) {
        this.trabajos = trabajos;
        this.lineasOrden = lineasOrden;
        this.trabajadores = trabajadores;
    }

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
        Trabajador trabajador = trabajadores.findById(r.idTrabajador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Trabajador", r.idTrabajador()));
        if (!trabajador.isActivo()) {
            throw new IllegalArgumentException("El trabajador " + trabajador.getNombre() + " está inactivo.");
        }
        LineaOrden linea = buscarLineaOrden(r.idOrdenDetalle());
        long yaReportado = trabajos.unidadesReportadas(r.idOrdenDetalle(), r.etapa(), EstadoTrabajo.RECHAZADO);
        long disponible = linea.getCantidadAProducir() - yaReportado;
        if (r.cantidad() > disponible) {
            throw new IllegalArgumentException("La cantidad supera lo pendiente en la etapa " + r.etapa()
                    + ": la línea pide " + linea.getCantidadAProducir() + ", ya se reportaron " + yaReportado
                    + " y quedan " + Math.max(disponible, 0) + ".");
        }
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

    @Transactional(readOnly = true)
    public List<Trabajador> trabajadoresActivos() {
        return trabajadores.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public java.util.Map<Long, String> nombresTrabajadores() {
        java.util.Map<Long, String> nombres = new java.util.HashMap<>();
        trabajadores.findAll().forEach(t -> nombres.put(t.getId(), t.getNombre()));
        return nombres;
    }

    @Transactional(readOnly = true)
    public TrabajoResponse obtener(Long idTrabajo) {
        return TrabajoResponse.desde(buscar(idTrabajo));
    }

    @Transactional(readOnly = true)
    public List<TrabajoResponse> listarPorLineaOrden(Long idOrdenDetalle) {
        return trabajos.findByIdOrdenDetalleOrderByFechaTrabajoDesc(idOrdenDetalle).stream()
                .map(TrabajoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<TrabajoResponse> listarPorTrabajador(Long idTrabajador) {
        return trabajos.findByIdTrabajadorOrderByFechaTrabajoDesc(idTrabajador).stream()
                .map(TrabajoResponse::desde).toList();
    }

    // Al validar un trabajo de ACABADO, esas unidades cuentan como prendas terminadas de la orden.
    @Transactional
    public TrabajoResponse validar(Long idTrabajo) {
        TrabajoRealizado t = buscar(idTrabajo);
        exigirReportado(t, EstadoTrabajo.VALIDADO);
        if (t.getEtapa() == EtapaProduccion.ACABADO) {
            LineaOrden linea = buscarLineaOrden(t.getIdOrdenDetalle());
            linea.setCantidadTerminada(linea.getCantidadTerminada() + t.getCantidad());
            lineasOrden.save(linea);
        }
        t.setEstado(EstadoTrabajo.VALIDADO);
        t.setFechaValidacion(LocalDateTime.now());
        return TrabajoResponse.desde(trabajos.save(t));
    }

    @Transactional
    public TrabajoResponse rechazar(Long idTrabajo, String observaciones) {
        TrabajoRealizado t = buscar(idTrabajo);
        exigirReportado(t, EstadoTrabajo.RECHAZADO);
        t.setEstado(EstadoTrabajo.RECHAZADO);
        t.setFechaValidacion(LocalDateTime.now());
        if (observaciones != null && !observaciones.isBlank()) {
            t.setObservaciones(observaciones);
        }
        return TrabajoResponse.desde(trabajos.save(t));
    }

    // Solo un trabajo REPORTADO puede pasar a VALIDADO o RECHAZADO.
    private void exigirReportado(TrabajoRealizado t, EstadoTrabajo nuevo) {
        if (t.getEstado() != EstadoTrabajo.REPORTADO) {
            throw new TransicionInvalidaException("trabajo realizado", t.getEstado(), nuevo);
        }
    }

    private TrabajoRealizado buscar(Long id) {
        return trabajos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Trabajo realizado", id));
    }

    private LineaOrden buscarLineaOrden(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El detalle de orden es obligatorio.");
        }
        return lineasOrden.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Detalle de orden", id));
    }
}
