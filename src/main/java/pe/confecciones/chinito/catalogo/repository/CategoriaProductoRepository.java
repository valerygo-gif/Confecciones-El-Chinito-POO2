package pe.confecciones.chinito.catalogo.repository;

import pe.confecciones.chinito.catalogo.modelo.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long> {
    List<CategoriaProducto> findAllByOrderByNombreAsc();
}
