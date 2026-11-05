package pe.confecciones.chinito.ventas.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.ventas.dto.PagoRequest;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import java.math.BigDecimal;

// El pago necesita monto positivo, método y no puede superar el saldo pendiente.
@Component
public class PagoRegistrableValidator {

    public void validar(PagoRequest pago, BigDecimal saldo) {
        if (pago.monto() == null || pago.monto().signum() <= 0) {
            throw new ReglaVentaException("El monto debe ser mayor a cero.");
        }
        if (pago.metodo() == null) {
            throw new ReglaVentaException("Indique el método de pago.");
        }
        if (pago.monto().compareTo(saldo) > 0) {
            throw new ReglaVentaException("El monto (" + pago.monto() + ") supera el saldo pendiente (" + saldo + ").");
        }
    }
}
