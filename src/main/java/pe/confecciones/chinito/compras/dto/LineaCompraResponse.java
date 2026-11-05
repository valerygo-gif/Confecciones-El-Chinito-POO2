package pe.confecciones.chinito.compras.dto;

import pe.confecciones.chinito.compras.modelo.TipoItemCompra;
import java.math.BigDecimal;

public record LineaCompraResponse(TipoItemCompra tipo, Long idItem, String descripcion, BigDecimal cantidad,
                                  BigDecimal precioUnitario, BigDecimal subtotal) {
}
