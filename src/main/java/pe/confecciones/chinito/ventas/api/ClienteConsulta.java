package pe.confecciones.chinito.ventas.api;

import java.util.Map;
import java.util.Optional;

// Pedidos y otros módulos validan clientes sin conocer la entidad Cliente.
public interface ClienteConsulta {
    boolean existeCliente(Long idCliente);
    Optional<String> nombreCliente(Long idCliente);
    // id -> nombre, ordenado por nombre (para listas desplegables de otros módulos).
    Map<Long, String> listarNombres();
}
