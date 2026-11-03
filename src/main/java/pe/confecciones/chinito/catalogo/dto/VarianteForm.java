package pe.confecciones.chinito.catalogo.dto;

import java.math.BigDecimal;

public class VarianteForm {
    private Long tallaId;
    private String sku;
    private BigDecimal precio = BigDecimal.ZERO;
    private Integer stockMinimo = 0;

    public Long getTallaId() { return tallaId; }
    public String getSku() { return sku; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setTallaId(Long tallaId) { this.tallaId = tallaId; }
    public void setSku(String sku) { this.sku = sku; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
}
