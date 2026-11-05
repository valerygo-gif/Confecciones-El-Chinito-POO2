package pe.confecciones.chinito.compras.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pe.confecciones.chinito.compras.modelo.Compra;
import java.util.List;

// Solo lectura sobre las vistas de costos del esquema compartido (no crea ni modifica nada).
public interface CostoRepository extends Repository<Compra, Long> {

    // [id_variante, sku, producto, talla, precio_venta_referencial, costo_materiales_estimado, margen_estimado]
    @Query(value = "select id_variante, sku, producto, talla, precio_venta_referencial, "
            + "costo_materiales_estimado, margen_estimado from vw_costo_estimado_variante "
            + "order by producto, talla", nativeQuery = true)
    List<Object[]> costosEstimados();

    // [id_orden_detalle, id_orden, id_variante, cantidad_terminada, costo_materiales, costo_mano_obra, costo_total]
    @Query(value = "select id_orden_detalle, id_orden, id_variante, cantidad_terminada, costo_materiales, "
            + "costo_mano_obra, costo_total from vw_costo_real_produccion order by id_orden desc, id_orden_detalle",
            nativeQuery = true)
    List<Object[]> costosReales();
}
