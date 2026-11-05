package pe.confecciones.chinito.compras.api;

import java.util.Optional;

// Otros módulos consultan proveedores solo mediante esta interfaz.
public interface ProveedorConsulta {
    boolean existeProveedor(Long idProveedor);
    Optional<String> nombreProveedor(Long idProveedor);
}
