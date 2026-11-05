package pe.confecciones.chinito.compras.service;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.compras.dto.*;
import pe.confecciones.chinito.compras.modelo.*;
import pe.confecciones.chinito.compras.repository.CompraRepository;
import pe.confecciones.chinito.compras.repository.ProveedorRepository;
import pe.confecciones.chinito.compras.service.excepcion.ProveedorNoEncontradoException;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;
import pe.confecciones.chinito.compras.service.integracion.InventarioPuerto;
import pe.confecciones.chinito.compras.service.validador.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CompraServiceTest {
    private final CompraRepository compras = mock(CompraRepository.class);
    private final ProveedorRepository proveedores = mock(ProveedorRepository.class);
    private final InventarioPuerto inventario = mock(InventarioPuerto.class);
    private final CompraService servicio = new CompraServiceImpl(compras, proveedores, inventario,
            new CompraRegistrableValidator(inventario), new CompraRecibibleValidator(inventario),
            new CompraAnulableValidator());

    private void catalogoConDatos() {
        when(proveedores.findById(1L)).thenReturn(Optional.of(new Proveedor()));
        when(inventario.nombreMaterial(5L)).thenReturn(Optional.of("Tela"));
        when(inventario.nombreVariante(7L)).thenReturn(Optional.of("Blusa M"));
        when(compras.save(any(Compra.class))).thenAnswer(i -> i.getArgument(0));
    }

    private LineaCompraRequest tela(String cant, String precio) {
        return new LineaCompraRequest(TipoItemCompra.MATERIAL, 5L, new BigDecimal(cant), new BigDecimal(precio));
    }

    private Compra compraRegistrada() {
        Compra c = new Compra();
        c.setProveedor(new Proveedor());
        CompraDetalle m = new CompraDetalle();
        m.setIdMaterial(5L); m.setCantidad(new BigDecimal("10.500")); m.setPrecioUnitario(new BigDecimal("4.00"));
        CompraDetalle p = new CompraDetalle();
        p.setIdVariante(7L); p.setCantidad(new BigDecimal("3")); p.setPrecioUnitario(new BigDecimal("20.00"));
        c.agregar(m); c.agregar(p);
        when(compras.findById(1L)).thenReturn(Optional.of(c));
        when(compras.save(any(Compra.class))).thenAnswer(i -> i.getArgument(0));
        return c;
    }

    @Test
    void registraCompraYCalculaTotal() {
        catalogoConDatos();
        var r = servicio.registrar(new CompraRequest(1L, " F001-10 ", List.of(tela("10", "2.50"),
                new LineaCompraRequest(TipoItemCompra.PRODUCTO, 7L, new BigDecimal("2"), new BigDecimal("15.00")))));
        assertEquals(EstadoCompra.REGISTRADA, r.estado());
        assertEquals("F001-10", r.nroDocumento());
        assertEquals(0, new BigDecimal("55.00").compareTo(r.total()));
    }

    @Test
    void exigeProveedorExistente() {
        assertThrows(ProveedorNoEncontradoException.class,
                () -> servicio.registrar(new CompraRequest(99L, null, List.of(tela("1", "1")))));
    }

    @Test
    void exigeAlMenosUnaLinea() {
        catalogoConDatos();
        assertThrows(ReglaCompraException.class, () -> servicio.registrar(new CompraRequest(1L, null, List.of())));
    }

    @Test
    void rechazaCantidadCeroPrecioNegativoYRepetidos() {
        catalogoConDatos();
        assertThrows(ReglaCompraException.class,
                () -> servicio.registrar(new CompraRequest(1L, null, List.of(tela("0", "1")))));
        assertThrows(ReglaCompraException.class,
                () -> servicio.registrar(new CompraRequest(1L, null, List.of(tela("1", "-1")))));
        assertThrows(ReglaCompraException.class,
                () -> servicio.registrar(new CompraRequest(1L, null, List.of(tela("1", "1"), tela("2", "1")))));
    }

    @Test
    void productosSoloEnUnidadesEnteras() {
        catalogoConDatos();
        var linea = new LineaCompraRequest(TipoItemCompra.PRODUCTO, 7L, new BigDecimal("1.5"), BigDecimal.ONE);
        assertThrows(ReglaCompraException.class, () -> servicio.registrar(new CompraRequest(1L, null, List.of(linea))));
    }

    @Test
    void rechazaItemInexistente() {
        catalogoConDatos();
        var linea = new LineaCompraRequest(TipoItemCompra.MATERIAL, 404L, BigDecimal.ONE, BigDecimal.ONE);
        assertThrows(ReglaCompraException.class, () -> servicio.registrar(new CompraRequest(1L, null, List.of(linea))));
    }

    @Test
    void recibirIngresaMaterialesYProductosAlInventario() {
        Compra c = compraRegistrada();
        when(inventario.ubicacionActiva(3L)).thenReturn(true);
        var r = servicio.recibir(1L, new RecepcionRequest(3L));
        assertEquals(EstadoCompra.RECIBIDA, c.getEstado());
        assertEquals(EstadoCompra.RECIBIDA, r.estado());
        verify(inventario).ingresarMaterial(eq(5L), eq(3L), eq(new BigDecimal("10.500")), anyString());
        verify(inventario).ingresarProducto(eq(7L), eq(3L), eq(3), anyString());
    }

    @Test
    void noRecibeEnUbicacionInactiva() {
        Compra c = compraRegistrada();
        when(inventario.ubicacionActiva(3L)).thenReturn(false);
        assertThrows(ReglaCompraException.class, () -> servicio.recibir(1L, new RecepcionRequest(3L)));
        assertEquals(EstadoCompra.REGISTRADA, c.getEstado());
        verifyNoMoreInteractions(ignoreStubs(inventario));
    }

    @Test
    void noRecibeDosVeces() {
        Compra c = compraRegistrada();
        c.setEstado(EstadoCompra.RECIBIDA);
        when(inventario.ubicacionActiva(3L)).thenReturn(true);
        assertThrows(ReglaCompraException.class, () -> servicio.recibir(1L, new RecepcionRequest(3L)));
        verify(inventario, never()).ingresarMaterial(any(), any(), any(), any());
    }

    @Test
    void anulaSoloComprasRegistradas() {
        Compra c = compraRegistrada();
        assertEquals(EstadoCompra.ANULADA, servicio.anular(1L).estado());
        assertThrows(ReglaCompraException.class, () -> servicio.anular(1L));
        c.setEstado(EstadoCompra.RECIBIDA);
        assertThrows(ReglaCompraException.class, () -> servicio.anular(1L));
    }
}
