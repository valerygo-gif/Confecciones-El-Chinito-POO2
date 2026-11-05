package pe.confecciones.chinito.compras.dto;

import java.util.List;

public record CompraRequest(Long idProveedor, String nroDocumento, List<LineaCompraRequest> lineas) {
}
