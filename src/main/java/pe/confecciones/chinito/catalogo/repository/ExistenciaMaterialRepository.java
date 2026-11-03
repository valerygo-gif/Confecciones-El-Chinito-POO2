package pe.confecciones.chinito.catalogo.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import pe.confecciones.chinito.catalogo.modelo.*;
import java.util.List;
import java.util.Optional;

public interface ExistenciaMaterialRepository extends JpaRepository<ExistenciaMaterial, ExistenciaMaterialId> {
    @Modifying
    @Query(value = """
        INSERT INTO existencia_material (id_ubicacion, id_material, cantidad_fisica)
        VALUES (:ubicacionId, :itemId, 0)
        ON DUPLICATE KEY UPDATE cantidad_fisica = cantidad_fisica
        """, nativeQuery = true)
    void asegurarFila(@Param("ubicacionId") Long ubicacionId, @Param("itemId") Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ExistenciaMaterial e where e.id = :id")
    Optional<ExistenciaMaterial> bloquear(@Param("id") ExistenciaMaterialId id);

    @Query(value = """
        SELECT m.id_material, m.nombre, m.unidad_medida,
               COALESCE(SUM(e.cantidad_fisica), 0), m.stock_minimo
        FROM material m LEFT JOIN existencia_material e ON m.id_material = e.id_material
        GROUP BY m.id_material, m.nombre, m.unidad_medida, m.stock_minimo
        ORDER BY m.nombre
        """, nativeQuery = true)
    List<Object[]> resumenMateriales();
}
