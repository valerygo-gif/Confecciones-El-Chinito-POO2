package pe.confecciones.chinito.pedidos.repository;

import pe.confecciones.chinito.pedidos.modelo.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.confecciones.chinito.pedidos.modelo.EstadoTrabajo;
import pe.confecciones.chinito.pedidos.modelo.EtapaProduccion;
import java.util.List;

public interface TrabajoRealizadoRepository extends JpaRepository<TrabajoRealizado, Long> {
    List<TrabajoRealizado> findByIdOrdenDetalleOrderByFechaTrabajoDesc(Long idOrdenDetalle);
    List<TrabajoRealizado> findByIdTrabajadorOrderByFechaTrabajoDesc(Long idTrabajador);

    // Unidades ya reportadas (sin contar rechazadas) de una etapa en una línea de orden.
    @Query("select coalesce(sum(t.cantidad), 0) from TrabajoRealizado t "
            + "where t.idOrdenDetalle = :linea and t.etapa = :etapa and t.estado <> :excluido")
    long unidadesReportadas(@Param("linea") Long idOrdenDetalle, @Param("etapa") EtapaProduccion etapa,
                            @Param("excluido") EstadoTrabajo excluido);
}
