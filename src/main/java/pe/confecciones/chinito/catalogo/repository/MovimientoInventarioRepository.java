package pe.confecciones.chinito.catalogo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.confecciones.chinito.catalogo.modelo.MovimientoInventario;
import java.util.List;
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findTop20ByOrderByFechaDescIdDesc();
}
