package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.EstadoTrabajo;
import pe.confecciones.chinito.pedidos.modelo.EtapaProduccion;
import pe.confecciones.chinito.pedidos.modelo.TrabajoRealizado;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TrabajoResponse(Long id, Long idOrdenDetalle, Long idTrabajador, EtapaProduccion etapa,
                              LocalDate fechaTrabajo, Integer cantidad, BigDecimal tarifaPorUnidad,
                              EstadoTrabajo estado, LocalDateTime fechaValidacion, String observaciones) {
    public static TrabajoResponse desde(TrabajoRealizado t) {
        return new TrabajoResponse(t.getId(), t.getIdOrdenDetalle(), t.getIdTrabajador(), t.getEtapa(),
                t.getFechaTrabajo(), t.getCantidad(), t.getTarifaPorUnidad(), t.getEstado(),
                t.getFechaValidacion(), t.getObservaciones());
    }
}
