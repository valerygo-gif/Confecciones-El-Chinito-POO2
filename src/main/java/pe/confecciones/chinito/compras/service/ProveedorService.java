package pe.confecciones.chinito.compras.service;

import pe.confecciones.chinito.compras.dto.ProveedorRequest;
import pe.confecciones.chinito.compras.dto.ProveedorResponse;
import pe.confecciones.chinito.compras.modelo.Proveedor;
import java.util.List;

public interface ProveedorService {
    List<ProveedorResponse> listar(String buscar);
    Proveedor buscar(Long id);
    Proveedor guardar(Long id, ProveedorRequest datos);
    void eliminar(Long id);
}
