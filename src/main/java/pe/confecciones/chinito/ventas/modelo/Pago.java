package pe.confecciones.chinito.ventas.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class Pago {
    public enum Metodo { EFECTIVO, YAPE, PLIN, TRANSFERENCIA, DEPOSITO, TARJETA, OTRO }
    public enum Estado { CONFIRMADO, ANULADO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;
    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;
    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", nullable = false)
    private Metodo metodo;
    @Column(name = "cuenta_receptora", length = 100)
    private String cuentaReceptora;
    @Column(name = "referencia_operacion", length = 120)
    private String referenciaOperacion;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado = Estado.CONFIRMADO;
    @Column(name = "observaciones")
    private String observaciones;

    public Long getId() { return id; }
    public Long getIdPedido() { return idPedido; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public BigDecimal getMonto() { return monto; }
    public Metodo getMetodo() { return metodo; }
    public String getCuentaReceptora() { return cuentaReceptora; }
    public String getReferenciaOperacion() { return referenciaOperacion; }
    public Estado getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    public void setFechaPago(LocalDateTime f) { this.fechaPago = f; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setMetodo(Metodo metodo) { this.metodo = metodo; }
    public void setCuentaReceptora(String c) { this.cuentaReceptora = c; }
    public void setReferenciaOperacion(String r) { this.referenciaOperacion = r; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public void setObservaciones(String o) { this.observaciones = o; }
}
