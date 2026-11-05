package pe.confecciones.chinito.compras.dto;

public record ProveedorResponse(Long id, String nombre, String ruc, String telefono, String contacto,
                                String observaciones, boolean eliminable) {
}
