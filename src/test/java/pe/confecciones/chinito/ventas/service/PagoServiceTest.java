package pe.confecciones.chinito.ventas.service;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoDetalleResponse;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.modelo.TipoItemPedido;
import pe.confecciones.chinito.pedidos.modelo.TipoPedido;
import pe.confecciones.chinito.ventas.dto.PagoRequest;
import pe.confecciones.chinito.ventas.modelo.*;
import pe.confecciones.chinito.ventas.repository.PagoRepository;
import pe.confecciones.chinito.ventas.repository.VentaRepository;
import pe.confecciones.chinito.ventas.service.excepcion.PagoNoEncontradoException;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import pe.confecciones.chinito.ventas.service.validador.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PagoServiceTest {
    private final PagoRepository pagos = mock(PagoRepository.class);
    private final VentaRepository ventas = mock(VentaRepository.class);
    private final PedidoConsulta pedidos = mock(PedidoConsulta.class);
    private final PagoService servicio = new PagoServiceImpl(pagos, pedidos,
            new VentaActivaValidator(ventas, pedidos), new PagoRegistrableValidator(), new PagoAnulableValidator());

    private void pedidoDe100(EstadoPedido estado) {
        var detalle = new PedidoDetalleResponse(1L, TipoItemPedido.INDIVIDUAL, null, "Blusa", 2,
                new BigDecimal("50.00"), null);
        when(pedidos.buscarPedido(1L)).thenReturn(Optional.of(new PedidoResponse(1L, 7L,
                LocalDateTime.now(), null, TipoPedido.REGULAR, estado, null, List.of(detalle))));
    }

    private void ventaActiva() {
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.of(new Venta()));
    }

    private PagoRequest pago(String monto, MetodoPago metodo) {
        return new PagoRequest(monto == null ? null : new BigDecimal(monto), metodo, null, null, null);
    }

    @Test
    void saldoEsTotalMenosPagosConfirmados() {
        pedidoDe100(EstadoPedido.CONFIRMADO);
        Pago ok = new Pago(); ok.setMonto(new BigDecimal("30.00"));
        Pago anulado = new Pago(); anulado.setMonto(new BigDecimal("40.00")); anulado.setEstado(EstadoPago.ANULADO);
        when(pagos.findByIdPedidoOrderByFechaPagoDesc(1L)).thenReturn(List.of(ok, anulado));
        assertEquals(0, new BigDecimal("100.00").compareTo(servicio.total(1L)));
        assertEquals(0, new BigDecimal("30.00").compareTo(servicio.pagado(1L)));
        assertEquals(0, new BigDecimal("70.00").compareTo(servicio.saldo(1L)));
    }

    @Test
    void pagoValidoConVentaActivaSeGuarda() {
        pedidoDe100(EstadoPedido.CONFIRMADO);
        ventaActiva();
        when(pagos.findByIdPedidoOrderByFechaPagoDesc(1L)).thenReturn(List.of());
        servicio.registrar(1L, pago("40.00", MetodoPago.YAPE));
        verify(pagos).save(any(Pago.class));
    }

    @Test
    void pagoMayorAlSaldoSeRechaza() {
        pedidoDe100(EstadoPedido.CONFIRMADO);
        ventaActiva();
        when(pagos.findByIdPedidoOrderByFechaPagoDesc(1L)).thenReturn(List.of());
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L, pago("150.00", MetodoPago.YAPE)));
        verify(pagos, never()).save(any());
    }

    @Test
    void montoCeroOSinMetodoSeRechaza() {
        pedidoDe100(EstadoPedido.CONFIRMADO);
        ventaActiva();
        when(pagos.findByIdPedidoOrderByFechaPagoDesc(1L)).thenReturn(List.of());
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L, pago("0", MetodoPago.YAPE)));
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L, pago("10.00", null)));
        verify(pagos, never()).save(any());
    }

    @Test
    void pagoExigeVentaRegistrada() {
        pedidoDe100(EstadoPedido.CONFIRMADO);
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.empty());
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L, pago("10.00", MetodoPago.EFECTIVO)));
        verify(pagos, never()).save(any());
    }

    @Test
    void pagoRechazadoConVentaAnulada() {
        pedidoDe100(EstadoPedido.CONFIRMADO);
        Venta v = new Venta(); v.setEstado(EstadoVenta.ANULADA);
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.of(v));
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L, pago("10.00", MetodoPago.EFECTIVO)));
    }

    @Test
    void pagoRechazadoSiElPedidoFueCancelado() {
        pedidoDe100(EstadoPedido.CANCELADO);
        ventaActiva();
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L, pago("10.00", MetodoPago.EFECTIVO)));
    }

    @Test
    void anularPagoLoMarcaAnulado() {
        Pago p = new Pago();
        when(pagos.findById(5L)).thenReturn(Optional.of(p));
        servicio.anular(5L);
        assertEquals(EstadoPago.ANULADO, p.getEstado());
        verify(pagos).save(p);
    }

    @Test
    void noSeAnulaDosVecesNiUnPagoInexistente() {
        Pago p = new Pago(); p.setEstado(EstadoPago.ANULADO);
        when(pagos.findById(5L)).thenReturn(Optional.of(p));
        when(pagos.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ReglaVentaException.class, () -> servicio.anular(5L));
        assertThrows(PagoNoEncontradoException.class, () -> servicio.anular(9L));
    }
}
