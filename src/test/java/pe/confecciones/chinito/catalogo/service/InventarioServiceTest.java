package pe.confecciones.chinito.catalogo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.confecciones.chinito.catalogo.modelo.*;
import pe.confecciones.chinito.catalogo.repository.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {
    @Mock ExistenciaProductoRepository existencias;
    @Mock ExistenciaMaterialRepository existenciaMaterial;
    @Mock MovimientoInventarioRepository movimientos;
    @Mock MovimientoMaterialRepository movimientosMaterial;
    @Mock ProductoVarianteRepository variantes;
    @Mock MaterialRepository materiales;
    @Mock UbicacionRepository ubicaciones;

    InventarioService servicio;

    @BeforeEach
    void preparar() {
        servicio = new InventarioService(existencias, existenciaMaterial, movimientos,
                movimientosMaterial, variantes, materiales, ubicaciones);
        Producto producto = new Producto();
        ProductoVariante variante = mock(ProductoVariante.class);
        when(variante.getId()).thenReturn(2L);
        when(variante.isActivo()).thenReturn(true);
        when(variante.getProducto()).thenReturn(producto);
        when(variantes.findById(2L)).thenReturn(Optional.of(variante));
        Ubicacion ubicacion = mock(Ubicacion.class);
        when(ubicacion.getId()).thenReturn(1L);
        when(ubicacion.isActiva()).thenReturn(true);
        when(ubicaciones.findById(1L)).thenReturn(Optional.of(ubicacion));
    }

    @Test
    void noPermiteDescontarStockReservado() {
        ExistenciaProducto fila = new ExistenciaProducto();
        fila.setCantidadFisica(10);
        when(existencias.bloquear(any())).thenReturn(Optional.of(fila));
        when(existencias.totalReservadoEnUbicacion(2L, 1L)).thenReturn(8);

        assertThrows(IllegalArgumentException.class, () -> servicio.moverProducto(
                2L, 1L, MovimientoInventario.Tipo.SALIDA, 3, "Venta"));

        verify(existencias, never()).saveAndFlush(any());
        verify(movimientos, never()).save(any());
    }

    @Test
    void ingresoAumentaStockYRegistraMovimiento() {
        ExistenciaProducto fila = new ExistenciaProducto();
        fila.setCantidadFisica(4);
        when(existencias.bloquear(any())).thenReturn(Optional.of(fila));
        when(existencias.totalReservadoEnUbicacion(2L, 1L)).thenReturn(0);

        servicio.moverProducto(2L, 1L, MovimientoInventario.Tipo.INGRESO, 6, "Producción");

        assertEquals(10, fila.getCantidadFisica());
        verify(existencias).saveAndFlush(fila);
        verify(movimientos).save(any(MovimientoInventario.class));
    }
}
