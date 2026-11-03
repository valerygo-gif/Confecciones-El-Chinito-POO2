package pe.confecciones.chinito.catalogo.dto;
import java.math.BigDecimal;
public record MaterialStockItem(Long materialId, String nombre, String unidad,
                                BigDecimal cantidadFisica, BigDecimal stockMinimo) {
    public boolean esBajo() { return cantidadFisica.compareTo(stockMinimo) < 0; }
}
