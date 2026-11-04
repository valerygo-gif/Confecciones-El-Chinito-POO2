package pe.confecciones.chinito.ventas.service;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoDetalleResponse;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.modelo.TipoItemPedido;
import pe.confecciones.chinito.pedidos.modelo.TipoPedido;
import pe.confecciones.chinito.ventas.modelo.Pago;
import pe.confecciones.chinito.ventas.modelo.Venta;
import pe.confecciones.chinito.ventas.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VentaServiceTest {
    private final VentaRepository ventas = mock(VentaRepository.class);
    private final PagoRepository pagos = mock(PagoRepository.class);
    private final EntregaRepository entregas = mock(EntregaRepository.class);
        private final PedidoConsulta pedidos = mock(PedidoConsulta.class);
    private final VentaService servicio = new VentaService(ventas, pagos, entregas, pedidos);

    private void pedidoDe100() {
        var detalle = new PedidoDetalleResponse(1L, TipoItemPedido.INDIVIDUAL, null, "Blusa", 2,
                new BigDecimal("50.00"), null);
        when(pedidos.buscarPedido(1L)).thenReturn(Optional.of(new PedidoResponse(1L, 7L,
                LocalDateTime.now(), null, TipoPedido.REGULAR, EstadoPedido.CONFIRMADO, null, List.of(detalle))));
    }

    @Test
    void saldoEsTotalMenosPagosConfirmados() {
        pedidoDe100();
        Pago ok = new Pago(); ok.setMonto(new BigDecimal("30.00"));
        Pago anulado = new Pago(); anulado.setMonto(new BigDecimal("40.00")); anulado.setEstado(Pago.Estado.ANULADO);
        when(pagos.findByIdPedidoOrderByFechaPagoDesc(1L)).thenReturn(List.of(ok, anulado));
        assertEquals(0, new BigDecimal("100.00").compareTo(servicio.total(1L)));
        assertEquals(0, new BigDecimal("30.00").compareTo(servicio.pagado(1L)));
    }

    @Test
    void pagoMayorAlSaldoSeRechaza() {
        pedidoDe100();
        when(pagos.findByIdPedidoOrderByFechaPagoDesc(1L)).thenReturn(List.of());
        ventaActiva();
        Pago f = new Pago();
        f.setMonto(new BigDecimal("150.00"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarPago(1L, f));
        verify(pagos, never()).save(any());
    }

    @Test
    void noSeVendeDosVecesElMismoPedido() {
        pedidoDe100();
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.of(new pe.confecciones.chinito.ventas.modelo.Venta()));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarVenta(1L));
    }

    private void ventaActiva() {
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.of(new Venta()));
    }

    @Test
    void pagoYEntregaExigenVentaRegistrada() {
        pedidoDe100();
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.empty());
        Pago f = new Pago(); f.setMonto(new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarPago(1L, f));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEntrega(1L, null));
        verify(pagos, never()).save(any());
        verify(entregas, never()).save(any());
    }

    @Test
    void pagoYEntregaRechazadosConVentaAnulada() {
        pedidoDe100();
        Venta v = new Venta(); v.setEstado(Venta.Estado.ANULADA);
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.of(v));
        Pago f = new Pago(); f.setMonto(new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarPago(1L, f));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEntrega(1L, null));
    }

    @Test
    void pagoValidoConVentaActivaSeGuarda() {
        pedidoDe100();
        ventaActiva();
        when(pagos.findByIdPedidoOrderByFechaPagoDesc(1L)).thenReturn(List.of());
        Pago f = new Pago(); f.setMonto(new BigDecimal("40.00"));
        servicio.registrarPago(1L, f);
        verify(pagos).save(f);
    }
}
