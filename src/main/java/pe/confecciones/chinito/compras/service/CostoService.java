package pe.confecciones.chinito.compras.service;

import pe.confecciones.chinito.compras.dto.CostoOrdenResponse;
import pe.confecciones.chinito.compras.dto.CostoVarianteResponse;
import pe.confecciones.chinito.compras.dto.ResumenProveedorResponse;
import java.util.List;

public interface CostoService {
    List<ResumenProveedorResponse> comprasPorProveedor();
    List<CostoVarianteResponse> costosEstimados();
    List<CostoOrdenResponse> costosReales();
}
