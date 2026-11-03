package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_material")
public class MovimientoMaterial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento") private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_ubicacion") private Ubicacion ubicacion;
    @ManyToOne(optional = false) @JoinColumn(name = "id_material") private Material material;
    @Column(nullable = false) private LocalDateTime fecha = LocalDateTime.now();
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private MovimientoInventario.Tipo tipo;
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;
    @Column(length = 255) private String motivo;
    public Long getId() { return id; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public Material getMaterial() { return material; }
    public LocalDateTime getFecha() { return fecha; }
    public MovimientoInventario.Tipo getTipo() { return tipo; }
    public BigDecimal getCantidad() { return cantidad; }
    public String getMotivo() { return motivo; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }
    public void setMaterial(Material material) { this.material = material; }
    public void setTipo(MovimientoInventario.Tipo tipo) { this.tipo = tipo; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
