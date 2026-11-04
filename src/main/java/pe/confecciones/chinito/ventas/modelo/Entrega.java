package pe.confecciones.chinito.ventas.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrega")
// El detalle por componente (entrega_detalle) depende de pedido_componente, aún no modelado en Pedidos.
public class Entrega {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long id;
    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;
    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega = LocalDateTime.now();
    @Column(name = "observaciones")
    private String observaciones;

    public Long getId() { return id; }
    public Long getIdPedido() { return idPedido; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public String getObservaciones() { return observaciones; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    public void setFechaEntrega(LocalDateTime f) { this.fechaEntrega = f; }
    public void setObservaciones(String o) { this.observaciones = o; }
}
