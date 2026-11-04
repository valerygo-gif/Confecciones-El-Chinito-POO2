package pe.confecciones.chinito.ventas.repository;

import pe.confecciones.chinito.ventas.modelo.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByIdPedidoOrderByFechaPagoDesc(Long idPedido);
}
