package pe.confecciones.chinito.pedidos.repository;

import pe.confecciones.chinito.pedidos.modelo.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LineaPedidoRepository extends JpaRepository<LineaPedido, Long> {
    List<LineaPedido> findByIdPedidoOrderByIdAsc(Long idPedido);
}
