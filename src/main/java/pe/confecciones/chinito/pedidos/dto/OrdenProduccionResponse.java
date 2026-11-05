package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.EstadoOrden;
import pe.confecciones.chinito.pedidos.modelo.LineaOrden;
import pe.confecciones.chinito.pedidos.modelo.OrdenProduccion;
import pe.confecciones.chinito.pedidos.modelo.TipoOrden;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenProduccionResponse(Long id, TipoOrden tipo, Long idPedido, LocalDateTime fechaCreacion,
                                      LocalDate fechaObjetivo, Integer prioridadManual, EstadoOrden estado,
                                      String indicaciones, List<LineaOrdenResponse> lineas) {
    public static OrdenProduccionResponse desde(OrdenProduccion o, List<LineaOrden> lineas) {
        return new OrdenProduccionResponse(o.getId(), o.getTipo(), o.getIdPedido(), o.getFechaCreacion(),
                o.getFechaObjetivo(), o.getPrioridadManual(), o.getEstado(), o.getIndicaciones(),
                lineas.stream().map(LineaOrdenResponse::desde).toList());
    }
}
