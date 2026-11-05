package pe.confecciones.chinito.ventas.service;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.ventas.dto.ClienteRequest;
import pe.confecciones.chinito.ventas.modelo.Cliente;
import pe.confecciones.chinito.ventas.modelo.TipoDocumento;
import pe.confecciones.chinito.ventas.repository.ClienteRepository;
import pe.confecciones.chinito.ventas.service.excepcion.ClienteNoEncontradoException;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;
import pe.confecciones.chinito.ventas.service.validador.ClienteDocumentoValidator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClienteServiceTest {
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final ClienteServiceImpl servicio = new ClienteServiceImpl(clientes, new ClienteDocumentoValidator(clientes));

    private ClienteRequest datos(String nombre, TipoDocumento tipo, String numero) {
        return new ClienteRequest(nombre, tipo, numero, null, null, null);
    }

    @Test
    void creaClienteSinDocumento() {
        servicio.guardar(null, datos("  Ana  ", null, " "));
        verify(clientes).save(argThat(c -> c.getNombre().equals("Ana") && c.getNumeroDocumento() == null));
    }

    @Test
    void nombreEsObligatorio() {
        assertThrows(ReglaVentaException.class, () -> servicio.guardar(null, datos("  ", null, null)));
        verify(clientes, never()).save(any());
    }

    @Test
    void tipoYNumeroVanJuntos() {
        assertThrows(ReglaVentaException.class, () -> servicio.guardar(null, datos("Ana", TipoDocumento.DNI, null)));
        assertThrows(ReglaVentaException.class, () -> servicio.guardar(null, datos("Ana", null, "12345678")));
    }

    @Test
    void dniYRucTienenLongitudFija() {
        assertThrows(ReglaVentaException.class, () -> servicio.guardar(null, datos("Ana", TipoDocumento.DNI, "123")));
        assertThrows(ReglaVentaException.class, () -> servicio.guardar(null, datos("Ana", TipoDocumento.RUC, "12345678")));
        servicio.guardar(null, datos("Ana", TipoDocumento.DNI, "12345678"));
        verify(clientes).save(any());
    }

    @Test
    void documentoDuplicadoSeRechaza() {
        when(clientes.existsByTipoDocumentoAndNumeroDocumento(TipoDocumento.DNI, "12345678")).thenReturn(true);
        assertThrows(ReglaVentaException.class, () -> servicio.guardar(null, datos("Ana", TipoDocumento.DNI, "12345678")));
        verify(clientes, never()).save(any());
    }

    @Test
    void editarSinCambiarDocumentoNoLoTomaPorDuplicado() {
        Cliente actual = new Cliente();
        actual.setTipoDocumento(TipoDocumento.DNI);
        actual.setNumeroDocumento("12345678");
        when(clientes.findById(4L)).thenReturn(Optional.of(actual));
        when(clientes.existsByTipoDocumentoAndNumeroDocumento(TipoDocumento.DNI, "12345678")).thenReturn(true);
        servicio.guardar(4L, datos("Ana María", TipoDocumento.DNI, "12345678"));
        assertEquals("Ana María", actual.getNombre());
    }

    @Test
    void editarClienteInexistente() {
        when(clientes.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ClienteNoEncontradoException.class, () -> servicio.guardar(9L, datos("Ana", null, null)));
    }
}
