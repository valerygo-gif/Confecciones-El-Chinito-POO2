package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="material")
public class Material {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_material")
    private Long id;
    @Column(nullable=false, length=140)
    private String nombre;
    @Column(name="unidad_medida", nullable=false, length=30)
    private String unidadMedida;
    @Column(name="costo_referencial", nullable=false, precision=12, scale=2)
    private BigDecimal costoReferencial = BigDecimal.ZERO;
    @Column(name="stock_minimo", nullable=false, precision=12, scale=3)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public BigDecimal getCostoReferencial() { return costoReferencial; }
    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public void setCostoReferencial(BigDecimal costoReferencial) { this.costoReferencial = costoReferencial; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }
}
