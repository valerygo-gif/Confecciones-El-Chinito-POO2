package pe.confecciones.chinito.pedidos.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "orden_produccion_detalle")
// id_variante (catálogo) e id_pedido_componente se guardan como Long simple.
public class OrdenProduccionDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_detalle")
    private Long id;
    @Column(name = "id_orden", nullable = false)
    private Long idOrden;
    @Column(name = "id_variante", nullable = false)
    private Long idVariante;
    @Column(name = "id_pedido_componente")
    private Long idPedidoComponente;
    @Column(name = "cantidad_a_producir", nullable = false)
    private Integer cantidadAProducir;
    @Column(name = "cantidad_terminada", nullable = false)
    private Integer cantidadTerminada = 0;

    public Long getId() { return id; }
    public Long getIdOrden() { return idOrden; }
    public Long getIdVariante() { return idVariante; }
    public Long getIdPedidoComponente() { return idPedidoComponente; }
    public Integer getCantidadAProducir() { return cantidadAProducir; }
    public Integer getCantidadTerminada() { return cantidadTerminada; }
    public void setIdOrden(Long idOrden) { this.idOrden = idOrden; }
    public void setIdVariante(Long idVariante) { this.idVariante = idVariante; }
    public void setIdPedidoComponente(Long id) { this.idPedidoComponente = id; }
    public void setCantidadAProducir(Integer cantidad) { this.cantidadAProducir = cantidad; }
    public void setCantidadTerminada(Integer cantidad) { this.cantidadTerminada = cantidad; }
}
