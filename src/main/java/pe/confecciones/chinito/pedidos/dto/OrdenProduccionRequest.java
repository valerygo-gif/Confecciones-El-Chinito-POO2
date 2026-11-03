package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.TipoOrden;
import java.time.LocalDate;
import java.util.List;

public record OrdenProduccionRequest(TipoOrden tipo, Long idPedido, LocalDate fechaObjetivo,
                                     Integer prioridadManual, String indicaciones,
                                     List<OrdenDetalleRequest> detalles) { }
