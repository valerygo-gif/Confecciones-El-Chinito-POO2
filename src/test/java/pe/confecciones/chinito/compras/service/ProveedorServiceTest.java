package pe.confecciones.chinito.compras.service;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.compras.dto.ProveedorRequest;
import pe.confecciones.chinito.compras.modelo.Proveedor;
import pe.confecciones.chinito.compras.repository.CompraRepository;
import pe.confecciones.chinito.compras.repository.ProveedorRepository;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;
import pe.confecciones.chinito.compras.service.validador.ProveedorDatosValidator;
import pe.confecciones.chinito.compras.service.validador.ProveedorEliminableValidator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProveedorServiceTest {
    private final ProveedorRepository repo = mock(ProveedorRepository.class);
    private final CompraRepository compras = mock(CompraRepository.class);
    private final ProveedorService servicio = new ProveedorServiceImpl(repo, compras,
            new ProveedorDatosValidator(repo), new ProveedorEliminableValidator(compras));

    private ProveedorRequest datos(String nombre, String ruc) {
        return new ProveedorRequest(nombre, ruc, " 999 ", null, null);
    }

    @Test
    void guardaProveedorValidoRecortandoTextos() {
        when(repo.save(any(Proveedor.class))).thenAnswer(i -> i.getArgument(0));
        Proveedor p = servicio.guardar(null, datos("  Telas SAC ", "20123456789"));
        assertEquals("Telas SAC", p.getNombre());
        assertEquals("999", p.getTelefono());
        assertNull(p.getContacto());
    }

    @Test
    void rechazaNombreVacio() {
        assertThrows(ReglaCompraException.class, () -> servicio.guardar(null, datos("  ", null)));
        verify(repo, never()).save(any());
    }

    @Test
    void rechazaRucMalFormado() {
        assertThrows(ReglaCompraException.class, () -> servicio.guardar(null, datos("Telas", "123")));
    }

    @Test
    void rechazaRucDuplicadoDeOtroProveedor() {
        Proveedor otro = mock(Proveedor.class);
        when(otro.getId()).thenReturn(9L);
        when(repo.findByRuc("20123456789")).thenReturn(Optional.of(otro));
        assertThrows(ReglaCompraException.class, () -> servicio.guardar(null, datos("Telas", "20123456789")));
    }

    @Test
    void noEliminaProveedorConCompras() {
        when(repo.findById(1L)).thenReturn(Optional.of(new Proveedor()));
        when(compras.existsByProveedor_Id(1L)).thenReturn(true);
        assertThrows(ReglaCompraException.class, () -> servicio.eliminar(1L));
        verify(repo, never()).delete(any());
    }

    @Test
    void eliminaProveedorSinCompras() {
        Proveedor p = new Proveedor();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        servicio.eliminar(1L);
        verify(repo).delete(p);
    }
}
