package pe.confecciones.chinito.catalogo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.confecciones.chinito.catalogo.modelo.MovimientoMaterial;
import java.util.List;
public interface MovimientoMaterialRepository extends JpaRepository<MovimientoMaterial, Long> {
    List<MovimientoMaterial> findTop20ByOrderByFechaDescIdDesc();
}
