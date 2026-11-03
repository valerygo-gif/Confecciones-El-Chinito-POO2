package pe.confecciones.chinito.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.confecciones.chinito.catalogo.modelo.Danza;
import java.util.List;

public interface DanzaRepository extends JpaRepository<Danza, Long> {
    List<Danza> findAllByOrderByNombreAsc();
    boolean existsByNombreIgnoreCaseAndRegion_Id(String nombre, Long regionId);
}
