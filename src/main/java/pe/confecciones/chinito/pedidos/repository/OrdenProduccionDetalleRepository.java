package pe.confecciones.chinito.pedidos.repository;

import pe.confecciones.chinito.pedidos.modelo.OrdenProduccionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenProduccionDetalleRepository extends JpaRepository<OrdenProduccionDetalle, Long> {
    List<OrdenProduccionDetalle> findByIdOrdenOrderByIdAsc(Long idOrden);
}
