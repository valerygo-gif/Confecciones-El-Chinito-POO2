package pe.confecciones.chinito.ventas.api;

import java.util.Optional;

// Pedidos y otros módulos validan clientes sin conocer la entidad Cliente.
public interface ClienteConsulta {
    boolean existeCliente(Long idCliente);
    Optional<String> nombreCliente(Long idCliente);
}
