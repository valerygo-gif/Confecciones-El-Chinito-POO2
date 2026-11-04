package pe.confecciones.chinito.ventas.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {
    public enum TipoDocumento { DNI, RUC, CE, OTRO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private TipoDocumento tipoDocumento;
    @Column(name = "numero_documento", length = 20)
    private String numeroDocumento;
    @Column(name = "nombre", nullable = false, length = 160)
    private String nombre;
    @Column(name = "telefono", length = 30)
    private String telefono;
    @Column(name = "email", length = 140)
    private String email;
    @Column(name = "direccion")
    private String direccion;

    public Long getId() { return id; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getDireccion() { return direccion; }
    public void setTipoDocumento(TipoDocumento t) { this.tipoDocumento = t; }
    public void setNumeroDocumento(String n) { this.numeroDocumento = n; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
