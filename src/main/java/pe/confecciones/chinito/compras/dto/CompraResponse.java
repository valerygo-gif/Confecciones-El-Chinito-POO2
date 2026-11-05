package pe.confecciones.chinito.compras.dto;

import pe.confecciones.chinito.compras.modelo.EstadoCompra;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponse(Long id, Long idProveedor, String proveedor, String nroDocumento,
                             LocalDateTime fecha, EstadoCompra estado, BigDecimal total,
                             List<LineaCompraResponse> lineas) {
}
