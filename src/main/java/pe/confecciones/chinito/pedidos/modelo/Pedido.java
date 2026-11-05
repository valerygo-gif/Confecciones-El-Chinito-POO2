package pe.confecciones.chinito.pedidos.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido")
// id_cliente pertenece a otro módulo, por eso se guarda como Long y no como relación.
public class Pedido {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido = LocalDateTime.now();

    @Column(name = "fecha_entrega_comprometida")
    private LocalDate fechaEntregaComprometida;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoPedido tipo = TipoPedido.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPedido estado = EstadoPedido.REGISTRADO;

    @Column(name = "notas", columnDefinition = "text")
    private String notas;

    public Long getId() { return id; }
    public Long getIdCliente() { return idCliente; }
    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public LocalDate getFechaEntregaComprometida() { return fechaEntregaComprometida; }
    public TipoPedido getTipo() { return tipo; }
    public EstadoPedido getEstado() { return estado; }
    public String getNotas() { return notas; }

    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }
    public void setFechaEntregaComprometida(LocalDate f) { this.fechaEntregaComprometida = f; }
    public void setTipo(TipoPedido tipo) { this.tipo = tipo; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public void setNotas(String notas) { this.notas = notas; }
}
