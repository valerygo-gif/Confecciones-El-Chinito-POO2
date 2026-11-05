package pe.confecciones.chinito.ventas.dto;

import pe.confecciones.chinito.ventas.modelo.Cliente;
import pe.confecciones.chinito.ventas.modelo.TipoDocumento;

public record ClienteRequest(String nombre, TipoDocumento tipoDocumento, String numeroDocumento,
                             String telefono, String email, String direccion) {
    public static ClienteRequest desde(Cliente c) {
        return new ClienteRequest(c.getNombre(), c.getTipoDocumento(), c.getNumeroDocumento(),
                c.getTelefono(), c.getEmail(), c.getDireccion());
    }

    // Recorta espacios y convierte los textos vacíos de nombre y documento en null.
    public ClienteRequest normalizado() {
        return new ClienteRequest(limpiar(nombre), tipoDocumento, limpiar(numeroDocumento), telefono, email, direccion);
    }

    private static String limpiar(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
