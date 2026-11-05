package pe.confecciones.chinito.ventas.service;

import pe.confecciones.chinito.ventas.dto.EntregaRequest;
import pe.confecciones.chinito.ventas.modelo.Entrega;
import java.util.List;

public interface EntregaService {
    List<Entrega> listar(Long idPedido);
    Entrega registrar(Long idPedido, EntregaRequest datos);
}
