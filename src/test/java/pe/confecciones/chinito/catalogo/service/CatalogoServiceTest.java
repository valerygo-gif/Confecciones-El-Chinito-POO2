package pe.confecciones.chinito.catalogo.service;

import org.junit.jupiter.api.Test;
import pe.confecciones.chinito.catalogo.dto.VarianteForm;
import pe.confecciones.chinito.catalogo.modelo.*;
import pe.confecciones.chinito.catalogo.repository.*;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogoServiceTest {
    @Test
    void accesorioSinTallaUtilizaEtiquetaUnica() {
        ProductoRepository productos = mock(ProductoRepository.class);
        ProductoVarianteRepository variantes = mock(ProductoVarianteRepository.class);
        CategoriaProductoRepository categorias = mock(CategoriaProductoRepository.class);
        DanzaRepository danzas = mock(DanzaRepository.class);
        TallaRepository tallas = mock(TallaRepository.class);
        InventarioService inventario = mock(InventarioService.class);
        CatalogoService servicio = new CatalogoService(productos, variantes,
                categorias, danzas, tallas, inventario);

        Producto producto = new Producto();
        producto.setTipo(Producto.TipoProducto.ACCESORIO);
        when(productos.findById(4L)).thenReturn(Optional.of(producto));
        Talla unica = mock(Talla.class);
        when(unica.getId()).thenReturn(7L);
        when(tallas.findByEtiquetaIgnoreCase("Única")).thenReturn(Optional.of(unica));
        when(variantes.save(any(ProductoVariante.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VarianteForm form = new VarianteForm();
        form.setPrecio(new BigDecimal("12.50"));
        form.setStockMinimo(2);

        ProductoVariante creada = servicio.crearVariante(4L, form);

        assertSame(unica, creada.getTalla());
        assertSame(producto, creada.getProducto());
        verify(variantes).save(creada);
    }
}
