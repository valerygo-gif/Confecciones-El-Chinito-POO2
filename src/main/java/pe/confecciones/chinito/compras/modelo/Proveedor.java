package pe.confecciones.chinito.compras.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "proveedor")
public class Proveedor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long id;
    @Column(nullable = false, length = 160)
    private String nombre;
    @Column(length = 11, unique = true)
    private String ruc;
    @Column(length = 30)
    private String telefono;
    @Column(length = 120)
    private String contacto;
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRuc() { return ruc; }
    public String getTelefono() { return telefono; }
    public String getContacto() { return contacto; }
    public String getObservaciones() { return observaciones; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
