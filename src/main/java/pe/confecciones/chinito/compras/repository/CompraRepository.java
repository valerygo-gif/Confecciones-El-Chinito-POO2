package pe.confecciones.chinito.compras.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.confecciones.chinito.compras.modelo.Compra;
import java.math.BigDecimal;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findAllByOrderByFechaCompraDesc();
    boolean existsByProveedor_Id(Long idProveedor);

    // [idProveedor, nombre, cantidad de compras, monto total] de compras ya recibidas.
    @Query("select c.proveedor.id, c.proveedor.nombre, count(distinct c.id), sum(d.cantidad * d.precioUnitario) "
            + "from Compra c join c.detalles d where c.estado = pe.confecciones.chinito.compras.modelo.EstadoCompra.RECIBIDA "
            + "group by c.proveedor.id, c.proveedor.nombre order by sum(d.cantidad * d.precioUnitario) desc")
    List<Object[]> resumenPorProveedor();

    @Query("select coalesce(sum(d.cantidad * d.precioUnitario), 0) from Compra c join c.detalles d "
            + "where c.estado = pe.confecciones.chinito.compras.modelo.EstadoCompra.RECIBIDA")
    BigDecimal totalRecibido();
}
