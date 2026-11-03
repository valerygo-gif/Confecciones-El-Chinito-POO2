package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "existencia_material")
public class ExistenciaMaterial {
    @EmbeddedId private ExistenciaMaterialId id;
    @Column(name = "cantidad_fisica", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadFisica = BigDecimal.ZERO;
    public ExistenciaMaterialId getId() { return id; }
    public BigDecimal getCantidadFisica() { return cantidadFisica; }
    public void setId(ExistenciaMaterialId id) { this.id = id; }
    public void setCantidadFisica(BigDecimal cantidadFisica) { this.cantidadFisica = cantidadFisica; }
}
