package pe.confecciones.chinito.ventas.service;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.pedidos.api.PedidoConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.modelo.TipoPedido;
import pe.confecciones.chinito.ventas.dto.EntregaRequest;
import pe.confecciones.chinito.ventas.modelo.Entrega;
import pe.confecciones.chinito.ventas.modelo.Venta;
import pe.confecciones.chinito.ventas.repository.EntregaRepository;
import pe.confecciones.chinito.ventas.repository.VentaRepository;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import pe.confecciones.chinito.ventas.service.validador.VentaActivaValidator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EntregaServiceTest {
    private final EntregaRepository entregas = mock(EntregaRepository.class);
    private final VentaRepository ventas = mock(VentaRepository.class);
    private final PedidoConsulta pedidos = mock(PedidoConsulta.class);
    private final EntregaService servicio =
            new EntregaServiceImpl(entregas, new VentaActivaValidator(ventas, pedidos), pedidos);

    private void pedidoEn(EstadoPedido estado) {
        when(pedidos.buscarPedido(1L)).thenReturn(Optional.of(new PedidoResponse(1L, 7L,
                LocalDateTime.now(), null, TipoPedido.REGULAR, estado, null, List.of())));
        when(ventas.findByIdPedido(1L)).thenReturn(Optional.of(new Venta()));
        when(entregas.save(any(Entrega.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void entregaUnPedidoListoOConEntregaParcial() {
        for (EstadoPedido estado : List.of(EstadoPedido.LISTO, EstadoPedido.ENTREGA_PARCIAL)) {
            pedidoEn(estado);
            assertEquals("ok", servicio.registrar(1L, new EntregaRequest("ok")).getObservaciones());
        }
    }

    @Test
    void rechazaEntregarUnPedidoQueNoEstaListo() {
        for (EstadoPedido estado : List.of(EstadoPedido.REGISTRADO, EstadoPedido.CONFIRMADO,
                EstadoPedido.EN_PRODUCCION, EstadoPedido.ENTREGADO)) {
            pedidoEn(estado);
            assertThrows(ReglaVentaException.class, () -> servicio.registrar(1L, new EntregaRequest("x")));
        }
        verify(entregas, never()).save(any());
    }
}
