package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
// Cada existencia se identifica por dos datos: la ubicación y la variante del producto.
public class ExistenciaProductoId implements Serializable {
    @Column(name="id_ubicacion")
    private Long ubicacionId;
    @Column(name="id_variante")
    private Long varianteId;

    public ExistenciaProductoId() { }
    public ExistenciaProductoId(Long ubicacionId, Long varianteId) {
        this.ubicacionId = ubicacionId;
        this.varianteId = varianteId;
    }
    public Long getUbicacionId() { return ubicacionId; }
    public Long getVarianteId() { return varianteId; }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ExistenciaProductoId other)) return false;
        return Objects.equals(ubicacionId, other.ubicacionId) && Objects.equals(varianteId, other.varianteId);
    }
    @Override public int hashCode() { return Objects.hash(ubicacionId, varianteId); }
}
