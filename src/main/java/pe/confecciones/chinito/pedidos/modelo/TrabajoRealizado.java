package pe.confecciones.chinito.pedidos.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trabajo_realizado")
// Una actividad debe estar VALIDADA antes de entrar al cálculo de comisiones.
public class TrabajoRealizado {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trabajo")
    private Long id;

    @Column(name = "id_orden_detalle", nullable = false)
    private Long idOrdenDetalle;

    @Column(name = "id_trabajador", nullable = false)
    private Long idTrabajador;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", nullable = false)
    private EtapaProduccion etapa;

    @Column(name = "fecha_trabajo", nullable = false)
    private LocalDate fechaTrabajo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "tarifa_por_unidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaPorUnidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTrabajo estado = EstadoTrabajo.REPORTADO;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    public Long getId() { return id; }
    public Long getIdOrdenDetalle() { return idOrdenDetalle; }
    public Long getIdTrabajador() { return idTrabajador; }
    public EtapaProduccion getEtapa() { return etapa; }
    public LocalDate getFechaTrabajo() { return fechaTrabajo; }
    public Integer getCantidad() { return cantidad; }
    public BigDecimal getTarifaPorUnidad() { return tarifaPorUnidad; }
    public EstadoTrabajo getEstado() { return estado; }
    public LocalDateTime getFechaValidacion() { return fechaValidacion; }
    public String getObservaciones() { return observaciones; }

    public void setIdOrdenDetalle(Long idOrdenDetalle) { this.idOrdenDetalle = idOrdenDetalle; }
    public void setIdTrabajador(Long idTrabajador) { this.idTrabajador = idTrabajador; }
    public void setEtapa(EtapaProduccion etapa) { this.etapa = etapa; }
    public void setFechaTrabajo(LocalDate fechaTrabajo) { this.fechaTrabajo = fechaTrabajo; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public void setTarifaPorUnidad(BigDecimal tarifa) { this.tarifaPorUnidad = tarifa; }
    public void setEstado(EstadoTrabajo estado) { this.estado = estado; }
    public void setFechaValidacion(LocalDateTime f) { this.fechaValidacion = f; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
