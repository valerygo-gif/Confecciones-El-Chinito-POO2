package pe.confecciones.chinito.catalogo.repository;

import pe.confecciones.chinito.catalogo.modelo.Talla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TallaRepository extends JpaRepository<Talla, Long> {
    List<Talla> findAllByOrderByOrdenVisualAsc();
    Optional<Talla> findByEtiquetaIgnoreCase(String etiqueta);
}
