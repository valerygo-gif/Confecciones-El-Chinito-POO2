package pe.confecciones.chinito.compras.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compra")
public class Compra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;
    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra = LocalDateTime.now();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCompra estado = EstadoCompra.REGISTRADA;
    @Column(name = "nro_documento", length = 60)
    private String nroDocumento;
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraDetalle> detalles = new ArrayList<>();

    public Long getId() { return id; }
    public Proveedor getProveedor() { return proveedor; }
    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public EstadoCompra getEstado() { return estado; }
    public String getNroDocumento() { return nroDocumento; }
    public List<CompraDetalle> getDetalles() { return detalles; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public void setFechaCompra(LocalDateTime fechaCompra) { this.fechaCompra = fechaCompra; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; }
    public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }

    public void agregar(CompraDetalle detalle) {
        detalle.setCompra(this);
        detalles.add(detalle);
    }

    public BigDecimal getTotal() {
        return detalles.stream().map(CompraDetalle::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
