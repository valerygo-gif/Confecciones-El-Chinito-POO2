package pe.confecciones.chinito.pedidos.service.validador;

import pe.confecciones.chinito.pedidos.modelo.EstadoTrabajo;
import pe.confecciones.chinito.pedidos.service.excepcion.TransicionEstadoInvalidaException;
import org.springframework.stereotype.Component;

// Solo un trabajo REPORTADO puede pasar a VALIDADO o RECHAZADO.
@Component
public class TrabajoPendienteValidator {
    public void validar(EstadoTrabajo actual, EstadoTrabajo nuevo) {
        if (actual != EstadoTrabajo.REPORTADO) {
            throw new TransicionEstadoInvalidaException("trabajo realizado", actual, nuevo);
        }
    }
}
