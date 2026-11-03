package pe.confecciones.chinito;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.catalogo.modelo.ExistenciaProductoId;
import static org.junit.jupiter.api.Assertions.*;

class ExistenciaProductoIdTest {
    @Test
    void dosClavesIgualesRepresentanLaMismaExistencia() {
        var a = new ExistenciaProductoId(1L, 2L);
        var b = new ExistenciaProductoId(1L, 2L);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new ExistenciaProductoId(1L, 3L));
    }
}
