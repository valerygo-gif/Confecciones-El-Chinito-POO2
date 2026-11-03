package pe.confecciones.chinito.catalogo.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import pe.confecciones.chinito.catalogo.modelo.ExistenciaProducto;
import pe.confecciones.chinito.catalogo.modelo.ExistenciaProductoId;
import java.util.List;
import java.util.Optional;

public interface ExistenciaProductoRepository extends JpaRepository<ExistenciaProducto, ExistenciaProductoId> {
    @Modifying
    @Query(value = """
        INSERT INTO existencia_producto (id_ubicacion, id_variante, cantidad_fisica)
        VALUES (:ubicacionId, :itemId, 0)
        ON DUPLICATE KEY UPDATE cantidad_fisica = cantidad_fisica
        """, nativeQuery = true)
    void asegurarFila(@Param("ubicacionId") Long ubicacionId, @Param("itemId") Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ExistenciaProducto e where e.id = :id")
    Optional<ExistenciaProducto> bloquear(@Param("id") ExistenciaProductoId id);

    // La vista resta las reservas activas del stock físico.
    @Query(value = """
        SELECT pv.id_variante, p.nombre, t.etiqueta, COALESCE(pv.sku,''),
               u.nombre, s.cantidad_fisica, s.cantidad_reservada, s.cantidad_disponible
        FROM vw_stock_disponible s
        JOIN producto_variante pv ON pv.id_variante = s.id_variante
        JOIN producto p ON p.id_producto = pv.id_producto
        JOIN talla t ON t.id_talla = pv.id_talla
        JOIN ubicacion u ON u.id_ubicacion = s.id_ubicacion
        ORDER BY p.nombre, t.orden_visual, u.nombre
        """, nativeQuery = true)
    List<Object[]> listarDisponibilidad();

    @Query(value = """
        SELECT COALESCE(SUM(s.cantidad_disponible),0)
        FROM vw_stock_disponible s WHERE s.id_variante = :idVariante
        """, nativeQuery = true)
    Number totalDisponible(@Param("idVariante") Long idVariante);

    @Query(value = """
        SELECT COALESCE(SUM(rs.cantidad),0) FROM reserva_stock rs
        JOIN pedido_componente pc ON pc.id_pedido_componente = rs.id_pedido_componente
        WHERE rs.estado = 'ACTIVA' AND rs.id_ubicacion = :ubicacionId
              AND pc.id_variante = :varianteId
        """, nativeQuery = true)
    Number totalReservadoEnUbicacion(@Param("varianteId") Long varianteId,
                                     @Param("ubicacionId") Long ubicacionId);
}
