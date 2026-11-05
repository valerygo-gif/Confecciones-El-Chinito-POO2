package pe.confecciones.chinito.compras.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "compra_detalle")
// Material y variante pertenecen al módulo Catálogo: se guardan solo como identificadores.
public class CompraDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra_detalle")
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_compra")
    private Compra compra;
    @Column(name = "id_material")
    private Long idMaterial;
    @Column(name = "id_variante")
    private Long idVariante;
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    public Long getId() { return id; }
    public Compra getCompra() { return compra; }
    public Long getIdMaterial() { return idMaterial; }
    public Long getIdVariante() { return idVariante; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setCompra(Compra compra) { this.compra = compra; }
    public void setIdMaterial(Long idMaterial) { this.idMaterial = idMaterial; }
    public void setIdVariante(Long idVariante) { this.idVariante = idVariante; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public TipoItemCompra getTipo() { return idMaterial != null ? TipoItemCompra.MATERIAL : TipoItemCompra.PRODUCTO; }
    public Long getIdItem() { return idMaterial != null ? idMaterial : idVariante; }
    public BigDecimal getSubtotal() { return cantidad.multiply(precioUnitario); }
}
