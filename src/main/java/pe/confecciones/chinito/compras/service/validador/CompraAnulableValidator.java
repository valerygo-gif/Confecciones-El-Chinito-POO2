package pe.confecciones.chinito.compras.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.compras.modelo.Compra;
import pe.confecciones.chinito.compras.modelo.EstadoCompra;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;

// Una compra recibida ya movió inventario: no se anula desde aquí.
@Component
public class CompraAnulableValidator {

    public void validar(Compra compra) {
        if (compra.getEstado() == EstadoCompra.ANULADA)
            throw new ReglaCompraException("La compra " + compra.getId() + " ya está anulada.");
        if (compra.getEstado() == EstadoCompra.RECIBIDA)
            throw new ReglaCompraException("La compra ya fue recibida e ingresó al inventario; no puede anularse.");
    }
}
