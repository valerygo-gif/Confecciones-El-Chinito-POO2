package pe.confecciones.chinito.compras.dto;

import pe.confecciones.chinito.compras.modelo.TipoItemCompra;
import java.math.BigDecimal;

public record LineaCompraRequest(TipoItemCompra tipo, Long idItem, BigDecimal cantidad, BigDecimal precioUnitario) {
}
