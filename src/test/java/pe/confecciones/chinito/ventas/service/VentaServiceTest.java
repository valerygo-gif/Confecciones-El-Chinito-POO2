package pe.confecciones.chinito.ventas.service;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.modelo.TipoPedido;
import pe.confecciones.chinito.ventas.api.ClienteConsulta;
import pe.confecciones.chinito.ventas.modelo.EstadoVenta;
import pe.confecciones.chinito.ventas.modelo.Venta;
import pe.confecciones.chinito.ventas.repository.VentaRepository;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import pe.confecciones.chinito.ventas.service.excepcion.VentaNoEncontradaException;
import pe.confecciones.chinito.ventas.service.validador.VentaAnulableValidator;
import pe.confecciones.chinito.ventas.service.validador.VentaRegistrableValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VentaServiceTest {
    private final VentaRepository ventas = mock(VentaRepository.class);
    private final PedidoConsulta pedidos = mock(PedidoConsulta.class);
    private final PagoService pagos = mock(PagoService.class);
    private final VentaServiceImpl servicio = new VentaServiceImpl(ventas, pedidos, mock(ClienteConsulta.class),
            pagos, mock(EntregaService.class), new VentaRegistrableValidator(ventas), new VentaAnulableValidator());

    private void pedido(EstadoPedido estado) {
        when(pedidos.buscarPedido(1L)).thenReturn(Optional.of(new PedidoResponse(1L, 7L,
                LocalDateTime.now(), null, TipoPedido.REGULAR, estado, null, List.of())));
    }

    @Test
    void registraVentaDePedidoVigente() {
        pedido(EstadoPedido.CONFIRMADO);
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.empty());
        servicio.registrar(1L);
        verify(ventas).save(any(Venta.class));
    }

    @Test
    void noSeVendeDosVecesElMismoPedido() {
        pedido(EstadoPedido.CONFIRMADO);
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.of(new Venta()));
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L));
        verify(ventas, never()).save(any());
    }

    @Test
    void noSeVendeUnPedidoCancelado() {
        pedido(EstadoPedido.CANCELADO);
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L));
    }

    @Test
    void noSeVendeUnPedidoInexistente() {
        when(pedidos.buscarPedido(1L)).thenReturn(Optional.empty());
        assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L));
    }

    @Test
    void anularVentaSinPagosLaMarcaAnulada() {
        Venta v = new Venta(); v.setIdPedido(1L);
        when(ventas.findById(3L)).thenReturn(Optional.of(v));
        when(pagos.pagado(1L)).thenReturn(BigDecimal.ZERO);
        servicio.anular(3L);
        assertEquals(EstadoVenta.ANULADA, v.getEstado());
        verify(ventas).save(v);
    }

    @Test
    void noSeAnulaUnaVentaConPagosConfirmados() {
        Venta v = new Venta(); v.setIdPedido(1L);
        when(ventas.findById(3L)).thenReturn(Optional.of(v));
        when(pagos.pagado(1L)).thenReturn(new BigDecimal("30.00"));
        assertThrows(ReglaVentaException.class, () -> servicio.anular(3L));
        assertEquals(EstadoVenta.ACTIVA, v.getEstado());
    }

    @Test
    void noSeAnulaDosVecesNiUnaVentaInexistente() {
        Venta v = new Venta(); v.setIdPedido(1L); v.setEstado(EstadoVenta.ANULADA);
        when(ventas.findById(3L)).thenReturn(Optional.of(v));
        when(ventas.findById(9L)).thenReturn(Optional.empty());
        when(pagos.pagado(1L)).thenReturn(BigDecimal.ZERO);
        assertThrows(ReglaVentaException.class, () -> servicio.anular(3L));
        assertThrows(VentaNoEncontradaException.class, () -> servicio.anular(9L));
    }

    @Test
    void ventaActivaSoloSiExisteYNoEstaAnulada() {
        Venta anulada = new Venta(); anulada.setEstado(EstadoVenta.ANULADA);
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.of(new Venta()));
        when(ventas.findByIdPedido(2L)).thenReturn(Optional.of(anulada));
        when(ventas.findByIdPedido(3L)).thenReturn(Optional.empty());
        assertTrue(servicio.tieneVentaActiva(1L));
        assertFalse(servicio.tieneVentaActiva(2L));
        assertFalse(servicio.tieneVentaActiva(3L));
    }
}
