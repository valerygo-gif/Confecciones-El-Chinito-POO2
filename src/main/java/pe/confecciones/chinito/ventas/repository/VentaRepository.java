package pe.confecciones.chinito.ventas.repository;

import pe.confecciones.chinito.ventas.modelo.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByIdPedido(Long idPedido);
    List<Venta> findAllByOrderByFechaVentaDesc();
}
