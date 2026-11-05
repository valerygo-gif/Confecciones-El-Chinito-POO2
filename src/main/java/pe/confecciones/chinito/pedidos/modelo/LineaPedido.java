package pe.confecciones.chinito.pedidos.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido_detalle")
// El precio acordado es una foto histórica: no depende del precio actual del catálogo.
public class LineaPedido {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido_detalle")
    private Long id;

    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false)
    private TipoItemPedido tipoItem;

    @Column(name = "id_conjunto")
    private Long idConjunto;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario_acordado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioAcordado;

    @Column(name = "referencia_foto_url", length = 500)
    private String referenciaFotoUrl;

    public Long getId() { return id; }
    public Long getIdPedido() { return idPedido; }
    public TipoItemPedido getTipoItem() { return tipoItem; }
    public Long getIdConjunto() { return idConjunto; }
    public String getDescripcion() { return descripcion; }
    public Integer getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitarioAcordado() { return precioUnitarioAcordado; }
    public String getReferenciaFotoUrl() { return referenciaFotoUrl; }

    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    public void setTipoItem(TipoItemPedido tipoItem) { this.tipoItem = tipoItem; }
    public void setIdConjunto(Long idConjunto) { this.idConjunto = idConjunto; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitarioAcordado(BigDecimal precio) { this.precioUnitarioAcordado = precio; }
    public void setReferenciaFotoUrl(String url) { this.referenciaFotoUrl = url; }
}
