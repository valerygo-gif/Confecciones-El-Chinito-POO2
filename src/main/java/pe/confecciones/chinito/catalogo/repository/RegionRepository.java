package pe.confecciones.chinito.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.confecciones.chinito.catalogo.modelo.Region;
import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findAllByOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
}
