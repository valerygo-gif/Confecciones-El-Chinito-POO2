package pe.confecciones.chinito.catalogo.dto;

public class ProductoForm {
    private String nombre;
    private Long categoriaId;
    private Long danzaId;
    private String tipo = "PRENDA";
    private String descripcion;

    public String getNombre() { return nombre; }
    public Long getCategoriaId() { return categoriaId; }
    public Long getDanzaId() { return danzaId; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public void setDanzaId(Long danzaId) { this.danzaId = danzaId; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
