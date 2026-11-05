package pe.confecciones.chinito.ventas.dto;

import pe.confecciones.chinito.ventas.modelo.MetodoPago;
import java.math.BigDecimal;

public record PagoRequest(BigDecimal monto, MetodoPago metodo, String cuentaReceptora,
                          String referenciaOperacion, String observaciones) {
}
