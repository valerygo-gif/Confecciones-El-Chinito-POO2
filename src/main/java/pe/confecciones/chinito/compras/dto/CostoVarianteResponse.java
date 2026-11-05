package pe.confecciones.chinito.compras.dto;

import java.math.BigDecimal;

public record CostoVarianteResponse(Long idVariante, String sku, String producto, String talla,
                                    BigDecimal precioVenta, BigDecimal costoMateriales, BigDecimal margen) {
}
