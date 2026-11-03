package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.pedidos.dto.OrdenProduccionRequest;
import pe.confecciones.chinito.pedidos.dto.OrdenProduccionResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoOrden;
import java.util.List;

public interface OrdenProduccionService {
    OrdenProduccionResponse crear(OrdenProduccionRequest request);
    OrdenProduccionResponse obtener(Long idOrden);
    List<OrdenProduccionResponse> listar(EstadoOrden estado);
    OrdenProduccionResponse cambiarEstado(Long idOrden, EstadoOrden nuevoEstado);
}
