package pe.confecciones.chinito.ventas.repository;

import pe.confecciones.chinito.ventas.modelo.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByIdPedidoOrderByFechaEntregaDesc(Long idPedido);
}
