package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name="danza")
public class Danza {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_danza")
    private Long id;
    @ManyToOne
    @JoinColumn(name="id_region")
    private Region region;
    @Column(nullable=false, length=120)
    private String nombre;

    public Long getId() { return id; }
    public Region getRegion() { return region; }
    public String getNombre() { return nombre; }
    public void setRegion(Region region) { this.region = region; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
