package pe.confecciones.chinito.pedidos.repository;

import pe.confecciones.chinito.pedidos.modelo.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findAllByOrderByFechaPedidoDesc();
    List<Pedido> findByEstadoOrderByFechaEntregaComprometidaAsc(EstadoPedido estado);
}
