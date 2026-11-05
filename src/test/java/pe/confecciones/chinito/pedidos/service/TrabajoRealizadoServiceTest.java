package pe.confecciones.chinito.pedidos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.pedidos.dto.TrabajoRequest;
import pe.confecciones.chinito.pedidos.modelo.*;
import pe.confecciones.chinito.pedidos.repository.LineaOrdenRepository;
import pe.confecciones.chinito.pedidos.repository.TrabajadorRepository;
import pe.confecciones.chinito.pedidos.repository.TrabajoRealizadoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrabajoRealizadoServiceTest {
    private final TrabajoRealizadoRepository trabajos = mock(TrabajoRealizadoRepository.class);
    private final LineaOrdenRepository lineas = mock(LineaOrdenRepository.class);
    private final TrabajadorRepository trabajadores = mock(TrabajadorRepository.class);
    private final TrabajoRealizadoService servicio = new TrabajoRealizadoService(trabajos, lineas, trabajadores);

    @BeforeEach
    void preparar() {
        Trabajador activo = mock(Trabajador.class);
        when(activo.isActivo()).thenReturn(true);
        when(trabajadores.findById(1L)).thenReturn(Optional.of(activo));
        Trabajador inactivo = mock(Trabajador.class);
        when(inactivo.isActivo()).thenReturn(false);
        when(inactivo.getNombre()).thenReturn("Luis");
        when(trabajadores.findById(5L)).thenReturn(Optional.of(inactivo));
        LineaOrden linea = new LineaOrden();
        linea.setCantidadAProducir(10);
        when(lineas.findById(3L)).thenReturn(Optional.of(linea));
        when(trabajos.save(any(TrabajoRealizado.class))).thenAnswer(i -> i.getArgument(0));
    }

    private TrabajoRequest pedido(Long trabajador, int cantidad) {
        return new TrabajoRequest(3L, trabajador, EtapaProduccion.CORTE, LocalDate.now(), cantidad,
                new BigDecimal("2.50"), null);
    }

    @Test
    void aceptaTrabajoDentroDeLoPendiente() {
        when(trabajos.unidadesReportadas(3L, EtapaProduccion.CORTE, EstadoTrabajo.RECHAZADO)).thenReturn(6L);
        assertEquals(4, servicio.reportar(pedido(1L, 4)).cantidad());
    }

    @Test
    void rechazaTrabajoQueSuperaLoPendienteDeLaEtapa() {
        when(trabajos.unidadesReportadas(3L, EtapaProduccion.CORTE, EstadoTrabajo.RECHAZADO)).thenReturn(6L);
        var e = assertThrows(IllegalArgumentException.class, () -> servicio.reportar(pedido(1L, 5)));
        assertTrue(e.getMessage().contains("quedan 4"));
        verify(trabajos, never()).save(any());
    }

    @Test
    void rechazaTrabajadorInactivoOInexistente() {
        assertThrows(IllegalArgumentException.class, () -> servicio.reportar(pedido(5L, 1)));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.reportar(pedido(99L, 1)));
        verify(trabajos, never()).save(any());
    }
}
