package pe.confecciones.chinito.compras.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.confecciones.chinito.compras.modelo.Proveedor;
import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findAllByOrderByNombreAsc();
    List<Proveedor> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
    Optional<Proveedor> findByRuc(String ruc);
}
