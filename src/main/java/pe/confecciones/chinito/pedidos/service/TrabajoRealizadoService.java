package pe.confecciones.chinito.pedidos.service;

import pe.confecciones.chinito.pedidos.dto.TrabajoRequest;
import pe.confecciones.chinito.pedidos.dto.TrabajoResponse;
import java.util.List;

public interface TrabajoRealizadoService {
    TrabajoResponse reportar(TrabajoRequest request);
    TrabajoResponse obtener(Long idTrabajo);
    List<TrabajoResponse> listarPorOrdenDetalle(Long idOrdenDetalle);
    List<TrabajoResponse> listarPorTrabajador(Long idTrabajador);
    TrabajoResponse validar(Long idTrabajo);
    TrabajoResponse rechazar(Long idTrabajo, String observaciones);
}
