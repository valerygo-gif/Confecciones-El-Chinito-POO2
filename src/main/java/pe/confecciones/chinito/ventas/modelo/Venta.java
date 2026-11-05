package pe.confecciones.chinito.ventas.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
// Una venta por pedido (id_pedido es UNIQUE). El pedido es de otro módulo: se guarda como Long.
public class Venta {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;
    @Column(name = "id_pedido", nullable = false, unique = true)
    private Long idPedido;
    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta = LocalDateTime.now();
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoVenta estado = EstadoVenta.ACTIVA;

    public Long getId() { return id; }
    public Long getIdPedido() { return idPedido; }
    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public EstadoVenta getEstado() { return estado; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    public void setFechaVenta(LocalDateTime f) { this.fechaVenta = f; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }
}
