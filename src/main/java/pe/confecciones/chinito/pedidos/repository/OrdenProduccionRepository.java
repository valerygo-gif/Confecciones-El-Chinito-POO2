package pe.confecciones.chinito.pedidos.repository;

import pe.confecciones.chinito.pedidos.modelo.EstadoOrden;
import pe.confecciones.chinito.pedidos.modelo.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, Long> {
    List<OrdenProduccion> findAllByOrderByFechaObjetivoAsc();
    List<OrdenProduccion> findByEstadoOrderByFechaObjetivoAsc(EstadoOrden estado);
    List<OrdenProduccion> findByIdPedidoOrderByFechaCreacionAsc(Long idPedido);
    boolean existsByIdPedidoAndEstadoNot(Long idPedido, EstadoOrden estado);
}
