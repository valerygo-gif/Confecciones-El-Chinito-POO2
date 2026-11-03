package pe.confecciones.chinito.catalogo.repository;

import pe.confecciones.chinito.catalogo.modelo.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    List<Ubicacion> findAllByOrderByNombreAsc();
}
