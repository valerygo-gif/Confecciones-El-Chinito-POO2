package pe.confecciones.chinito.catalogo.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {
    public enum Tipo { INGRESO, SALIDA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, TRASLADO_ENTRADA, TRASLADO_SALIDA }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_ubicacion")
    private Ubicacion ubicacion;
    @ManyToOne(optional = false) @JoinColumn(name = "id_variante")
    private ProductoVariante variante;
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Tipo tipo;
    @Column(nullable = false)
    private Integer cantidad;
    @Column(length = 255)
    private String motivo;

    public Long getId() { return id; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public ProductoVariante getVariante() { return variante; }
    public LocalDateTime getFecha() { return fecha; }
    public Tipo getTipo() { return tipo; }
    public Integer getCantidad() { return cantidad; }
    public String getMotivo() { return motivo; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }
    public void setVariante(ProductoVariante variante) { this.variante = variante; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
