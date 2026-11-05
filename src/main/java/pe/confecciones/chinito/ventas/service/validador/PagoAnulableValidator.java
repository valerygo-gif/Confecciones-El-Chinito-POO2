package pe.confecciones.chinito.ventas.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.ventas.modelo.EstadoPago;
import pe.confecciones.chinito.ventas.modelo.Pago;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;

@Component
public class PagoAnulableValidator {

    public void validar(Pago pago) {
        if (pago.getEstado() == EstadoPago.ANULADO) {
            throw new ReglaVentaException("El pago " + pago.getId() + " ya está anulado.");
        }
    }
}
