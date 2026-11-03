package pe.confecciones.chinito.catalogo.dto;

import java.math.BigDecimal;

public record VarianteInfo(
        Long id, String producto, String talla, String sku,
        BigDecimal precioReferencial, int stockDisponible) {
}
