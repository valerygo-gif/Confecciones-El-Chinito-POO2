package pe.confecciones.chinito.pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.confecciones.chinito.pedidos.modelo.Trabajador;
import java.util.List;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    List<Trabajador> findByActivoTrueOrderByNombreAsc();
}
