package pe.confecciones.chinito.compras.dto;

import java.math.BigDecimal;

public record ResumenProveedorResponse(Long idProveedor, String proveedor, long compras, BigDecimal monto) {
}
