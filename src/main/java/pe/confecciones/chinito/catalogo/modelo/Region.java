package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name="region")
public class Region {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_region")
    private Long id;
    @Column(nullable=false, length=60)
    private String nombre;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
