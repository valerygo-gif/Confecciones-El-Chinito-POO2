package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="producto_variante")
// Cada variante representa una talla de un producto, con su SKU, precio y stock mínimo.
public class ProductoVariante {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_variante")
    private Long id;
    @ManyToOne(optional=false)
    @JoinColumn(name="id_producto", nullable=false)
    private Producto producto;
    @ManyToOne(optional=false)
    @JoinColumn(name="id_talla", nullable=false)
    private Talla talla;
    @Column(length=70)
    private String sku;
    @Column(name="precio_venta_referencial", nullable=false, precision=12, scale=2)
    private BigDecimal precioVentaReferencial = BigDecimal.ZERO;
    @Column(name="stock_minimo", nullable=false)
    private Integer stockMinimo = 0;
    @Column(nullable=false)
    private boolean activo = true;

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public Talla getTalla() { return talla; }
    public String getSku() { return sku; }
    public BigDecimal getPrecioVentaReferencial() { return precioVentaReferencial; }
    public Integer getStockMinimo() { return stockMinimo; }
    public boolean isActivo() { return activo; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public void setTalla(Talla talla) { this.talla = talla; }
    public void setSku(String sku) { this.sku = sku; }
    public void setPrecioVentaReferencial(BigDecimal precio) { this.precioVentaReferencial = precio; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
