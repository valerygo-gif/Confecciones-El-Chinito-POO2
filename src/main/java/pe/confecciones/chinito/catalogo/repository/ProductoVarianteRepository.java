package pe.confecciones.chinito.catalogo.repository;

import pe.confecciones.chinito.catalogo.modelo.ProductoVariante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {
    List<ProductoVariante> findByProducto_IdOrderByTalla_OrdenVisualAsc(Long productoId);
    boolean existsByProducto_IdAndTalla_Id(Long productoId, Long tallaId);
    boolean existsBySkuIgnoreCase(String sku);
    Optional<ProductoVariante> findBySkuIgnoreCase(String sku);
}
