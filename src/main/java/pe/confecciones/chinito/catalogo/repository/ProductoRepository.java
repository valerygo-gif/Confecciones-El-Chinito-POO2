package pe.confecciones.chinito.catalogo.repository;

import pe.confecciones.chinito.catalogo.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findAllByOrderByNombreAsc();
    List<Producto> findByNombreContainingIgnoreCaseOrderByNombreAsc(String texto);
}
