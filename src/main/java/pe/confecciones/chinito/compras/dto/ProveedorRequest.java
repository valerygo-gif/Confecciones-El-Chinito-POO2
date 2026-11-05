package pe.confecciones.chinito.compras.dto;

public record ProveedorRequest(String nombre, String ruc, String telefono, String contacto, String observaciones) {

    // Recorta espacios y convierte los textos vacíos en null.
    public ProveedorRequest normalizado() {
        return new ProveedorRequest(limpio(nombre), limpio(ruc), limpio(telefono), limpio(contacto), limpio(observaciones));
    }

    private static String limpio(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
