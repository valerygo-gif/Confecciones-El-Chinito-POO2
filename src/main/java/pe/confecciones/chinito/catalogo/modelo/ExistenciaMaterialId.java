package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExistenciaMaterialId implements Serializable {
    @Column(name = "id_ubicacion") private Long ubicacionId;
    @Column(name = "id_material") private Long materialId;
    public ExistenciaMaterialId() {}
    public ExistenciaMaterialId(Long ubicacionId, Long materialId) {
        this.ubicacionId = ubicacionId;
        this.materialId = materialId;
    }
    public Long getUbicacionId() { return ubicacionId; }
    public Long getMaterialId() { return materialId; }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ExistenciaMaterialId id)) return false;
        return Objects.equals(ubicacionId, id.ubicacionId) && Objects.equals(materialId, id.materialId);
    }
    @Override public int hashCode() { return Objects.hash(ubicacionId, materialId); }
}
