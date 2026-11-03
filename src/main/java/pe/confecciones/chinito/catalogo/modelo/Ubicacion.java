package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name="ubicacion")
public class Ubicacion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_ubicacion")
    private Long id;
    @Column(nullable=false, length=100)
    private String nombre;
    @Column(length=255)
    private String descripcion;
    @Column(nullable=false)
    private boolean activa = true;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public boolean isActiva() { return activa; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
