package pe.confecciones.chinito.ventas.repository;

import pe.confecciones.chinito.ventas.modelo.Cliente;
import pe.confecciones.chinito.ventas.modelo.Cliente.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
    List<Cliente> findAllByOrderByNombreAsc();
    boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumento tipo, String numero);
}
