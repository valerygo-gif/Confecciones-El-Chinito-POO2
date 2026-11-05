package pe.confecciones.chinito.pedidos.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "orden_produccion")
// tipo PEDIDO exige id_pedido; tipo REPOSICION exige que sea nulo (regla ck_op_pedido_o_stock de la BD).
public class OrdenProduccion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoOrden tipo = TipoOrden.PEDIDO;

    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_objetivo", nullable = false)
    private LocalDate fechaObjetivo;

    // 1 = más urgente, 5 = menos urgente. La columna es TINYINT.
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "prioridad_manual")
    private Integer prioridadManual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @Column(name = "indicaciones", columnDefinition = "text")
    private String indicaciones;

    public Long getId() { return id; }
    public TipoOrden getTipo() { return tipo; }
    public Long getIdPedido() { return idPedido; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDate getFechaObjetivo() { return fechaObjetivo; }
    public Integer getPrioridadManual() { return prioridadManual; }
    public EstadoOrden getEstado() { return estado; }
    public String getIndicaciones() { return indicaciones; }

    public void setTipo(TipoOrden tipo) { this.tipo = tipo; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }
    public void setFechaObjetivo(LocalDate fechaObjetivo) { this.fechaObjetivo = fechaObjetivo; }
    public void setPrioridadManual(Integer prioridadManual) { this.prioridadManual = prioridadManual; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
}
