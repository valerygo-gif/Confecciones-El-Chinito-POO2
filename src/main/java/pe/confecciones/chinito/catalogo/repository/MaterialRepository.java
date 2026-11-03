package pe.confecciones.chinito.catalogo.repository;

import pe.confecciones.chinito.catalogo.modelo.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findAllByOrderByNombreAsc();
}
