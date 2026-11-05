package pe.confecciones.chinito.pedidos.modelo;

import jakarta.persistence.*;

// Solo lectura: se usa para comprobar que el trabajador existe y está activo al reportar trabajo.
@Entity
@Table(name = "trabajador")
public class Trabajador {
    @Id
    @Column(name = "id_trabajador")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public boolean isActivo() { return activo; }
}
