package pe.confecciones.chinito.compras.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.compras.dto.ProveedorRequest;
import pe.confecciones.chinito.compras.modelo.Proveedor;
import pe.confecciones.chinito.compras.repository.ProveedorRepository;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;

// Nombre obligatorio, largos máximos del esquema, RUC de 11 dígitos y sin repetirse.
@Component
public class ProveedorDatosValidator {
    private final ProveedorRepository proveedores;

    public ProveedorDatosValidator(ProveedorRepository proveedores) { this.proveedores = proveedores; }

    public void validar(ProveedorRequest r, Proveedor actual) {
        if (r.nombre() == null) throw new ReglaCompraException("El nombre del proveedor es obligatorio.");
        if (r.nombre().length() > 160) throw new ReglaCompraException("El nombre admite hasta 160 caracteres.");
        if (r.telefono() != null && r.telefono().length() > 30)
            throw new ReglaCompraException("El teléfono admite hasta 30 caracteres.");
        if (r.contacto() != null && r.contacto().length() > 120)
            throw new ReglaCompraException("El contacto admite hasta 120 caracteres.");
        if (r.ruc() == null) return;
        if (!r.ruc().matches("\\d{11}")) throw new ReglaCompraException("El RUC debe tener 11 dígitos.");
        proveedores.findByRuc(r.ruc()).ifPresent(otro -> {
            if (actual == null || !otro.getId().equals(actual.getId()))
                throw new ReglaCompraException("Ya existe un proveedor con el RUC " + r.ruc() + ".");
        });
    }
}
