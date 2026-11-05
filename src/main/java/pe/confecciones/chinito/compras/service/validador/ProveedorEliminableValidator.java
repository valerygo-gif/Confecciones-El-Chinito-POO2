package pe.confecciones.chinito.compras.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.compras.repository.CompraRepository;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;

// Un proveedor con compras registradas conserva su historial y no se elimina.
@Component
public class ProveedorEliminableValidator {
    private final CompraRepository compras;

    public ProveedorEliminableValidator(CompraRepository compras) { this.compras = compras; }

    public void validar(Long idProveedor) {
        if (compras.existsByProveedor_Id(idProveedor))
            throw new ReglaCompraException("El proveedor tiene compras registradas y no puede eliminarse.");
    }
}
