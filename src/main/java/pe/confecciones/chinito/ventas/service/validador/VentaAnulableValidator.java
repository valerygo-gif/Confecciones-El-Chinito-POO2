package pe.confecciones.chinito.ventas.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.ventas.modelo.EstadoVenta;
import pe.confecciones.chinito.ventas.modelo.Venta;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import java.math.BigDecimal;

// No se anula una venta ya anulada ni una que aún tiene pagos confirmados.
@Component
public class VentaAnulableValidator {

    public void validar(Venta venta, BigDecimal pagado) {
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new ReglaVentaException("La venta " + venta.getId() + " ya está anulada.");
        }
        if (pagado.signum() > 0) {
            throw new ReglaVentaException("La venta tiene pagos confirmados (" + pagado + "): anúlelos primero.");
        }
    }
}
