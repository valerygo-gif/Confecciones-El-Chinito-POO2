package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name="talla")
public class Talla {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_talla")
    private Long id;
    @Column(nullable=false, length=25)
    private String etiqueta;
    @Column(name="orden_visual", nullable=false)
    private Integer ordenVisual = 0;

    public Long getId() { return id; }
    public String getEtiqueta() { return etiqueta; }
    public Integer getOrdenVisual() { return ordenVisual; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }
}
