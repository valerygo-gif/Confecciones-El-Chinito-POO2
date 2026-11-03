package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name="existencia_producto")
public class ExistenciaProducto {
    @EmbeddedId
    private ExistenciaProductoId id;
    @Column(name="cantidad_fisica", nullable=false)
    private Integer cantidadFisica = 0;

    public ExistenciaProductoId getId() { return id; }
    public Integer getCantidadFisica() { return cantidadFisica; }
    public void setId(ExistenciaProductoId id) { this.id = id; }
    public void setCantidadFisica(Integer cantidadFisica) { this.cantidadFisica = cantidadFisica; }
}
