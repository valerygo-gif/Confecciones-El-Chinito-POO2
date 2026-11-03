package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name="producto")
public class Producto {
    public enum TipoProducto { PRENDA, ACCESORIO }

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_producto")
    private Long id;
    @ManyToOne(optional=false)
    @JoinColumn(name="id_categoria", nullable=false)
    private CategoriaProducto categoria;
    @ManyToOne
    @JoinColumn(name="id_danza")
    private Danza danza;
    @Column(nullable=false, length=150)
    private String nombre;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TipoProducto tipo = TipoProducto.PRENDA;
    @Column(columnDefinition="text")
    private String descripcion;
    @Column(nullable=false)
    private boolean activo = true;

    public Long getId() { return id; }
    public CategoriaProducto getCategoria() { return categoria; }
    public Danza getDanza() { return danza; }
    public String getNombre() { return nombre; }
    public TipoProducto getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public boolean isActivo() { return activo; }
    public void setCategoria(CategoriaProducto categoria) { this.categoria = categoria; }
    public void setDanza(Danza danza) { this.danza = danza; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(TipoProducto tipo) { this.tipo = tipo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
