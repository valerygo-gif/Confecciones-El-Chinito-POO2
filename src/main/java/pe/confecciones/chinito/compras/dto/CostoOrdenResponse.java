package pe.confecciones.chinito.compras.dto;

import java.math.BigDecimal;

public record CostoOrdenResponse(Long idOrdenDetalle, Long idOrden, Long idVariante, int cantidadTerminada,
                                 BigDecimal costoMateriales, BigDecimal costoManoObra, BigDecimal costoTotal) {
}
