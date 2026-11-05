package pe.confecciones.chinito.compras.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.compras.dto.CompraRequest;
import pe.confecciones.chinito.compras.dto.LineaCompraRequest;
import pe.confecciones.chinito.compras.modelo.TipoItemCompra;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;
import pe.confecciones.chinito.compras.service.integracion.InventarioPuerto;
import java.util.HashSet;
import java.util.Set;

// Reglas para registrar una compra: al menos una línea, ítems existentes y sin repetir,
// cantidades y precios acordes al esquema (productos en unidades enteras).
@Component
public class CompraRegistrableValidator {
    private final InventarioPuerto inventario;

    public CompraRegistrableValidator(InventarioPuerto inventario) { this.inventario = inventario; }

    public void validar(CompraRequest r) {
        if (r.nroDocumento() != null && r.nroDocumento().length() > 60)
            throw new ReglaCompraException("El número de documento admite hasta 60 caracteres.");
        if (r.lineas() == null || r.lineas().isEmpty())
            throw new ReglaCompraException("La compra necesita al menos un ítem.");
        Set<String> vistos = new HashSet<>();
        for (LineaCompraRequest l : r.lineas()) {
            validarLinea(l);
            if (!vistos.add(l.tipo() + ":" + l.idItem()))
                throw new ReglaCompraException("El ítem " + l.idItem() + " está repetido en la compra.");
        }
    }

    private void validarLinea(LineaCompraRequest l) {
        if (l.tipo() == null || l.idItem() == null) throw new ReglaCompraException("Selecciona el ítem a comprar.");
        boolean existe = l.tipo() == TipoItemCompra.MATERIAL
                ? inventario.nombreMaterial(l.idItem()).isPresent()
                : inventario.nombreVariante(l.idItem()).isPresent();
        if (!existe) throw new ReglaCompraException("El ítem " + l.idItem() + " no existe o está inactivo.");
        if (l.cantidad() == null || l.cantidad().signum() <= 0)
            throw new ReglaCompraException("La cantidad debe ser mayor que cero.");
        if (l.cantidad().scale() > 3 || l.cantidad().precision() - l.cantidad().scale() > 9)
            throw new ReglaCompraException("La cantidad no es válida.");
        if (l.tipo() == TipoItemCompra.PRODUCTO && l.cantidad().stripTrailingZeros().scale() > 0)
            throw new ReglaCompraException("Los productos se compran en unidades enteras.");
        if (l.precioUnitario() == null || l.precioUnitario().signum() < 0
                || l.precioUnitario().scale() > 2 || l.precioUnitario().precision() - l.precioUnitario().scale() > 10)
            throw new ReglaCompraException("El precio unitario no es válido.");
    }
}
