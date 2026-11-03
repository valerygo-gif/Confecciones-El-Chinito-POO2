package pe.confecciones.chinito.pedidos.repository;

import pe.confecciones.chinito.pedidos.modelo.TrabajoRealizado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrabajoRealizadoRepository extends JpaRepository<TrabajoRealizado, Long> {
    List<TrabajoRealizado> findByIdOrdenDetalleOrderByFechaTrabajoDesc(Long idOrdenDetalle);
    List<TrabajoRealizado> findByIdTrabajadorOrderByFechaTrabajoDesc(Long idTrabajador);
}
